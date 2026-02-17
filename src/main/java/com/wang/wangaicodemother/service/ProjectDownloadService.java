package com.wang.wangaicodemother.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {

    /**
     * 项目下载
     *
     * @param projectUrl
     * @param projectName
     * @param response
     * @return
     */
    void downloadProjectAsZip(String projectUrl, String projectName, HttpServletResponse response);
}
