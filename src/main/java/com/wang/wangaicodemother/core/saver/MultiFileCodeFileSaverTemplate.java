package com.wang.wangaicodemother.core.saver;

import com.wang.wangaicodemother.ai.model.MultiFileCodeResult;
import com.wang.wangaicodemother.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存器
 *
 * @author yupi
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaveTemplate<MultiFileCodeResult> {

    @Override
    public CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFile(String baseDirPath,MultiFileCodeResult result) {
        // 保存 HTML 文件
        writeFile(baseDirPath, "index.html", result.getHtmlCode());
        // 保存 CSS 文件
        writeFile(baseDirPath, "style.css", result.getCssCode());
        // 保存 JavaScript 文件
        writeFile(baseDirPath, "script.js", result.getJsCode());
    }

}
