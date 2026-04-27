package com.example.cjpagent.constant;

/**
 * 文件保存路径常量类，定义了应用中使用的各种文件路径常量，以便在代码中统一管理和使用这些路径，避免硬编码，提高代码的可维护性和可读性。
 */
public interface FileConstant {
    String FILE_SAVE_PATH = System.getProperty("user.dir") + "/tmp";
}
