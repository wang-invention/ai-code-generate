package com.wang.wangaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wang.wangaicodemother.ai.model.HtmlCodeResult;
import com.wang.wangaicodemother.ai.model.MultiFileCodeResult;
import com.wang.wangaicodemother.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存器
 */
public class CodeFileSaver {

    /**
     * 代码文件保存路径
     */
    private static final String CODE_FILE_PATH = System.getProperty("user.dir")+"/tmp/code_output";

    /**
     * 保存HTML代码文件
     * @param htmlCodeResult
     * @return
     */
    public static File saveHtmlFile(HtmlCodeResult htmlCodeResult) {
        //1.生成唯一的文件名
        String dirPath = generateFileName(CodeGenTypeEnum.HTML.getValue());
        writeFile(dirPath,"index.html",htmlCodeResult.getHtmlCode());
        return new File(dirPath);
    }

    public static File saveMultiFile(MultiFileCodeResult multiFileCodeResult) {
        //1.生成唯一的文件名
        String dirPath = generateFileName(CodeGenTypeEnum.HTML.getValue());
        writeFile(dirPath,"index.html",multiFileCodeResult.getHtmlCode());
        writeFile(dirPath,"index.css",multiFileCodeResult.getCssCode());
        writeFile(dirPath,"index.js",multiFileCodeResult.getJsCode());
        return new File(dirPath);
    }


    /**
     * 生成唯一文件
     * @param bizType
     * @return
     */
    private static String generateFileName(String bizType) {
        String uniqueFileName = StrUtil.format("{}_{}",bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath=CODE_FILE_PATH+File.separator+uniqueFileName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }


    /**
     * 写入文件
     */
    private static void writeFile(String dirPath,String filename,String content) {
        FileUtil.writeUtf8String(content,dirPath+File.separator+filename);
    }


}
