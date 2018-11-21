/*
 * report_writer.cpp
 *
 *  Created on: 2015-11-5
 *      Author: chenjinyi
 */
#include "report_writer.h"
#include <fcntl.h>
#include <stdarg.h>
#include <stdio.h>
#include <sys/stat.h>
#include <unistd.h>

ReportWriter::ReportWriter(const char *pathname) {
   open_success = OpenReportFile(pathname);
}

ReportWriter::~ReportWriter() {
    if(fd!=-1) close(fd);
}

bool ReportWriter::OpenReportFile(const char *pathname) {
    if((fd = open(pathname, O_RDWR|O_CREAT|O_APPEND, S_IRWXU|S_IRWXG|S_IRWXO))==-1) {
        return false;
    }else {
        unsigned long file_size = GetFileSize(pathname);
        if(file_size > MAX_FILE_SIZE || file_size < 0) {
            ftruncate(fd, 0);
            return true;
        }
    }
    return true;
}

void ReportWriter::Write(const char *content, ...) {
    if(open_success) {
        va_list argp;
        va_start(argp, content);
        char buf[BUF_SIZE];
        memset(buf, 0, BUF_SIZE);
        vsprintf(buf, content, argp);
        va_end(argp);
        write(fd, buf, strlen(buf));
    }
}

void ReportWriter::WriteString(const char *content) {
    if(open_success) {
        write(fd, content, strlen(content));
    }
}

unsigned long ReportWriter::GetFileSize(const char *pathname) {
    struct stat buf;
    if(stat(pathname, &buf)<0)
        return -1;
    return (unsigned long) buf.st_size;
}


