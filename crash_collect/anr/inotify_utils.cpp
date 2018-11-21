#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/inotify.h>
#include "event_queue.h"
#include "inotify_utils.h"
#include "exception_handler.h"

using namespace native_crash_collector;

#define BUFFER_LENGTH 1024
#define TARGET_FILE_MAX_SIZE 100000
#define ANR_IME_MAX_LINE_NUM 10

const char *anr_end_flag = "-- end";
const char *cmd_line_flag = "Cmd line";

extern int keep_running;
static int watched_items;
static int has_copy_anr_file = 0;

int open_inotify_fd() {
    //LOGD("open_inotify_fd");
    int fd;
    watched_items = 0;
    fd = inotify_init();
    if (fd < 0) {

    }
    return fd;
}

int close_inotify_fd(int fd) {
    int r;
    if ((r = close(fd)) < 0) {
    }
    watched_items = 0;
    return r;
}

int copy_anr_file(char * anr_file) {
    CRASH_LOGD("copy_anr_file");
    if (ExceptionHandler::anr_save_path == NULL ||
        ExceptionHandler::package_name == NULL ||
        !ExceptionHandler::collect_anr_on)
        return -1;
    has_copy_anr_file = 0;
    char *anr_file_path=new char [100];
    sprintf(anr_file_path,"%s/%s",ANR_FILE_DIR,anr_file);

    FILE *src_file = fopen(anr_file_path, "r");

    delete[] anr_file_path;
    FILE *target_file = fopen(ExceptionHandler::anr_save_path, "w");
    char data[BUFFER_LENGTH];
    size_t bytes_in, bytes_out;
    long len = 0;
    if (src_file == NULL) {
        return -1;
    }
    if (target_file == NULL) {
        return -1;
    }
    //LOGD("copy_anr_file start");
    bool hasWriteExtInfo = false;
    while (fgets(data, BUFFER_LENGTH, src_file) != NULL) {
        fputs(data, target_file);
        if (!hasWriteExtInfo && strstr(data, cmd_line_flag) &&
            strstr(data, ExceptionHandler::package_name)) {
            memset(data, 0, BUFFER_LENGTH);
            sprintf(data, "ime info : %s \n", ExceptionHandler::head_info);
            fputs(data, target_file);

            memset(data, 0, BUFFER_LENGTH);
            fputs(data, target_file);

            hasWriteExtInfo = true;
        }

        if (strstr(data, anr_end_flag)) {
            break;
        }
    }

    fclose(src_file);
    fclose(target_file);
    has_copy_anr_file = 1;
    return 0;
}

int has_anr_file_contain_ime_info(char* anr_file) {
    //LOGD("has_anr_file_contain_ime_info");
    if (ExceptionHandler::package_name == NULL || anr_file == NULL)
        return -1;

    char *anr_file_path=new char [100];
    sprintf(anr_file_path,"%s/%s",ANR_FILE_DIR,anr_file);

    CRASH_LOGD("has_anr_file_contain_ime_info  anr_file==%s  anr_file_path==%s", anr_file, anr_file_path);

    char buffer[BUFFER_LENGTH];
    FILE *pf = fopen(anr_file_path, "r");
    delete[] anr_file_path;
    if (pf == NULL) {
        return -1;
    }
    int ime_line_number = 0;
    while (fgets(buffer, BUFFER_LENGTH, pf) != NULL) {
        if (ime_line_number > ANR_IME_MAX_LINE_NUM) {
            break;
        }
        //LOGD("anr info : %s", buffer);
        if (strstr(buffer, ExceptionHandler::package_name)) {
            fclose(pf);
            return 0;
        } else {
            ime_line_number++;
        }
    }
    fclose(pf);
    return -1;
}

void handle_anr_file_close_write(char* anr_file) {
    //LOGD("handle_anr_file_close_write %d", has_copy_anr_file);
    if (has_copy_anr_file == 0) {
        if (has_anr_file_contain_ime_info(anr_file) == 0) {
            copy_anr_file(anr_file);
        }
    }
}

void handle_anr_file_create() {
    has_copy_anr_file = 0;
}

void handle_event(queue_entry_t event) {
    /* If the event was associated with a filename, we will store it here */
    char *cur_event_filename = NULL;
//    char *cur_event_file_or_dir = NULL;
    /* This is the watch descriptor the event occurred on */
    int cur_event_wd = event->inot_ev.wd;
    int cur_event_cookie = event->inot_ev.cookie;
    unsigned long flags;

    if (event->inot_ev.len) {
        cur_event_filename = event->inot_ev.name;
    }
    if (event->inot_ev.mask & IN_ISDIR) {
        return;
    }
    flags = event->inot_ev.mask &
            ~(IN_ALL_EVENTS | IN_UNMOUNT | IN_Q_OVERFLOW | IN_IGNORED);

    CRASH_LOGD("cur_event_filename==%s event==%d", cur_event_filename, event->inot_ev.mask);
    if (cur_event_filename == NULL || (!strstr(cur_event_filename, "anr") && !strstr(cur_event_filename, "traces"))) {
        CRASH_LOGD("cur_event_filename not match");
        return;
    }

    /* Perform event dependent handler routines */
    /* The mask is the magic that tells us what file operation occurred */
    switch (event->inot_ev.mask &
            (IN_ALL_EVENTS | IN_UNMOUNT | IN_Q_OVERFLOW | IN_IGNORED)) {
        /* File was accessed */
        case IN_ACCESS:
            //LOGD ("ACCESS: %s \"%s\" on WD #%i\n",
            //      cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            break;

            /* File was modified */
        case IN_MODIFY:
            //LOGD ("MODIFY: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            break;

            /* File changed attributes */
        case IN_ATTRIB:
            //LOGD ("ATTRIB: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            break;

            /* File open for writing was closed */
        case IN_CLOSE_WRITE:
            //LOGD ("CLOSE_WRITE: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            handle_anr_file_close_write(cur_event_filename);
            break;

            /* File open read-only was closed */
        case IN_CLOSE_NOWRITE:
            //LOGD ("CLOSE_NOWRITE: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);

            break;

            /* File was opened */
        case IN_OPEN:
            //LOGD ("OPEN: %s \"%s\" on WD #%i\n",
            //      cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            break;

            /* File was moved from X */
        case IN_MOVED_FROM:
            //LOGD ("MOVED_FROM: %s \"%s\" on WD #%i. Cookie=%d\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd,
            //     cur_event_cookie);
            break;

            /* File was moved to X */
        case IN_MOVED_TO:
            //LOGD ("MOVED_TO: %s \"%s\" on WD #%i. Cookie=%d\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd,
            //     cur_event_cookie);
            break;

            /* Subdir or file was deleted */
        case IN_DELETE:
            //LOGD ("DELETE: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            break;

            /* Subdir or file was created */
        case IN_CREATE:
            //LOGD ("CREATE: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            handle_anr_file_create();
            break;

            /* Watched entry was deleted */
        case IN_DELETE_SELF:
            //LOGD ("DELETE_SELF: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            break;

            /* Watched entry was moved */
        case IN_MOVE_SELF:
            //LOGD ("MOVE_SELF: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            break;

            /* Backing FS was unmounted */
        case IN_UNMOUNT:
            //LOGD ("UNMOUNT: %s \"%s\" on WD #%i\n",
            //     cur_event_file_or_dir, cur_event_filename, cur_event_wd);
            break;

            /* Too many FS events were received without reading them
               some event notifications were potentially lost.  */
        case IN_Q_OVERFLOW:
            //LOGD ("Warning: AN OVERFLOW EVENT OCCURRED: \n");
            break;

            /* Watch was removed explicitly by inotify_rm_watch or automatically
               because file was deleted, or file system was unmounted.  */
        case IN_IGNORED:
            watched_items--;
            //LOGD ("IGNORED: WD #%d\n", cur_event_wd);
            //LOGD("Watching = %d items\n",watched_items);
            break;

            /* Some unknown message received */
        default:
            //LOGD ("UNKNOWN EVENT \"%X\" OCCURRED for file \"%s\" on WD #%i\n",
            //    event->inot_ev.mask, cur_event_filename, cur_event_wd);
            break;
    }
    /* If any flags were set other than IN_ISDIR, report the flags */
    if (flags & (~IN_ISDIR)) {
        flags = event->inot_ev.mask;
        //LOGD ("Flags=%lX\n", flags);
    }
}

void handle_events(queue_t q) {
    queue_entry_t event;
    while (!queue_empty(q)) {
        event = queue_dequeue(q);
        handle_event(event);
        free(event);
    }
}

int read_events(queue_t q, int fd) {
    char buffer[16384];
    size_t buffer_i;
    struct inotify_event *pevent;
    queue_entry_t event;
    ssize_t r;
    size_t event_size, q_event_size;
    int count = 0;

    r = read(fd, buffer, 16384);
    if (r <= 0)
        return r;
    buffer_i = 0;
    while (buffer_i < r) {
        /* Parse events and queue them. */
        pevent = (struct inotify_event *) &buffer[buffer_i];
        event_size = offsetof (struct inotify_event, name) + pevent->len;
        q_event_size = offsetof (struct queue_entry, inot_ev.name) + pevent->len;
        event = (queue_entry_t) malloc(q_event_size);
        memmove(&(event->inot_ev), pevent, event_size);
        queue_enqueue(event, q);
        buffer_i += event_size;
        count++;
    }
    return count;
}

int event_check(int fd) {
    fd_set rfds;
    FD_ZERO (&rfds);
    FD_SET (fd, &rfds);

    return select(FD_SETSIZE, &rfds, NULL, NULL, NULL);
}

int process_inotify_events(queue_t q, int fd) {
    //LOGD("process_inotify_events");
    while (keep_running && (watched_items > 0)) {
        if (event_check(fd) > 0) {
            int r;
            r = read_events(q, fd);
            if (r < 0) {
                break;
            } else {
                handle_events(q);
            }
        }
    }
    return 0;
}

int watch_dir(int fd, const char *dirname, unsigned long mask) {
    int wd;
    wd = inotify_add_watch(fd, dirname, mask);
    if (wd < 0) {
        //LOGD ("Cannot add watch for \"%s\" with event mask %lX", dirname, mask);
    } else {
        watched_items++;
        //LOGD ("Watching %s WD=%d\n", dirname, wd);
        //LOGD ("Watching = %d items\n", watched_items);
    }
    CRASH_LOGD("watch_dir  success");
    return wd;
}

int ignore_wd(int fd, int wd) {
    int r;
    r = inotify_rm_watch(fd, wd);
    if (r < 0) {
        //LOGD("inotify_rm_watch(fd, wd) = ");
    } else {
        watched_items--;
    }
    return r;
}
