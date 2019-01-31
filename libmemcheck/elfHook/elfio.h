#ifndef ELFIO_H_
#define ELFIO_H_

struct ElfHandle {
	void *base;
	size_t space_size;
	bool fromfile;
};

/**
 * 从文件中打开ELF
 */
bool openElfByFile(const char *path, ElfHandle *pHandle);

/**
 * 释放资源
 */
void closeElfByFile(ElfHandle *handle);

/**
 * 从给定的so中获取基址, 如果soname为NULL，则表示当前进程自身
 */
bool openElfBySoname(const char *soname, ElfHandle *pHandle);

/**
 * 释放资源
 */
void closeElfBySoname(ElfHandle *handle);


#endif /* ELFIO_H_ */
