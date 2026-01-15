package com.wang.wangaicodemother.core.saver;

import com.wang.wangaicodemother.ai.model.HtmlCodeResult;
import com.wang.wangaicodemother.enums.CodeGenTypeEnum;

public class HtmlFileSaveTemplate extends CodeFileSaveTemplate<HtmlCodeResult>{
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFile(String baseDirPath, HtmlCodeResult result) {
        writeFile(baseDirPath,"index.html",result.getHtmlCode());
    }
}
