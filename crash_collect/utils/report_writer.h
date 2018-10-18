/*
 * report_writer.h
 *
 *  Created on: 2015-11-5
 *      Author: chenjinyi
 */

#ifndef REPORT_WRITER_H_
#define REPORT_WRITER_H_
#define MAX_FILE_SIZE 50*1024  //50kB

#define BUF_SIZE 255

class ReportWriter {
  public:
    ReportWriter(const char *pathname);
    ~ReportWriter();
    void Write(const char *content, ...);
    void WriteString(const char *content);
    bool open_success;
  private:
    int fd;
    bool OpenReportFile(const char *pathname);
    unsigned long GetFileSize(const char *pathname);
};

#endif /* REPORT_WRITER_H_ */
