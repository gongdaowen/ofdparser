# ofdparser
OFD电子发票验签、发票信息提取、保存为图片。

## 创建文档

    OFDInfo ofdInfo = OfdParser.parser(new File("/path/to/test.ofd"));

## 验签

    ofdInfo.verify()

## 发票信息提取

    ofdInfo.getInvoice()

## 保存为图片

    // 保存为一张图片
    ofdInfo.convertAsSingleImage(dpi);
    
    // 保存为多张图片（一页一张图片）
    ofdInfo.convertAsImage(dpi);

