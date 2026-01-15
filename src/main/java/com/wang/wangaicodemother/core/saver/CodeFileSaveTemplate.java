package com.wang.wangaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wang.wangaicodemother.enums.CodeGenTypeEnum;

import java.io.File;

/**
 *代码保存器的抽象类
 */
public abstract class CodeFileSaveTemplate<T> {

    /**
     * 代码文件保存路径
     */
    private static final String CODE_FILE_PATH = System.getProperty("user.dir")+"/tmp/code_output";

    public final File saveCode(T result) {
        //1. 验证输入
        validate(result);
        //2.构建唯一目录
        String baseDirPath=buildUniqueDirPath();
        //3.保存文件
        saveFile(baseDirPath,result);
        //4.返回文件对象
        return new File(baseDirPath);
    }



    private String buildUniqueDirPath() {
        String uniqueFileName = StrUtil.format("{}_{}",getCodeType().getValue(), IdUtil.getSnowflakeNextIdStr());
        String dirPath=CODE_FILE_PATH+File.separator+uniqueFileName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 校验文件内容，可由子类重写
     * @param result
     */
    protected void validate(T result) {
        if (result==null){
            throw new RuntimeException("输入不能为空");
        }
    }


    /**
     * 写入文件
     */
    protected static void writeFile(String dirPath,String filename,String content) {
        FileUtil.writeUtf8String(content,dirPath+File.separator+filename);
    }

    /**
     * 获取文件类型
     * @return
     */
    protected abstract CodeGenTypeEnum getCodeType();


    protected abstract void saveFile(String baseDirPath, T result);
}
