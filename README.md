# ofdparser
OFD电子发票验签、发票信息提取、保存为图片。

## 创建文档

    OFDInfo ofdInfo = OfdParser.parser(new File("/path/to/test.ofd"));

## 验签

    ofdInfo.verify()

## 发票信息提取

    ofdInfo.getInvoice()

## 保存为图片

    OFDPainter ofdPainter = ofdInfo.getOfdPainter();
    
    // 保存为一张图片
    BufferedImage image = ofdPainter.convertAsSingleImage(150);
    ImageIO.write(image, "JPG", new File(fileName + "-image-0-single" + ".jpg"));
    
    // 保存为多张图片（一页一张图片）
    List<BufferedImage> images = ofdPainter.convertAsImages(150);
    for (int i = 0; i < images.size(); i++) {
        ImageIO.write(images.get(i), "JPG", new File(fileName + "-image-" + (i + 1) + ".jpg"));
    }

## 保存为SVG矢量图

    OFDPainter ofdPainter = ofdInfo.getOfdPainter();
    
    // 保存为一张SVG
    SVGGraphics2D svg = ofdPainter.convertAsSingleSVG(150);
    svg.stream(new FileWriter(fileName + "-svg-0-single" + ".svg"));
    
    // 保存为多张图片（一页一个SVG）
    List<SVGGraphics2D> svgs = ofdPainter.convertAsSVGs(150);
    for (int i = 0; i < svgs.size(); i++) {
        svgs.get(i).stream(new FileWriter(fileName + "-svg-" + (i + 1) + ".svg"));
    }
