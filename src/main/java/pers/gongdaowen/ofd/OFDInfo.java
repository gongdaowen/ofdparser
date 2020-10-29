package pers.gongdaowen.ofd;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import pers.gongdaowen.ofd.model.*;
import pers.gongdaowen.ofd.utils.Base64Utils;
import pers.gongdaowen.ofd.utils.BeanUtils;
import pers.gongdaowen.ofd.utils.OfdUtils;
import sun.awt.image.BufferedImageGraphicsConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class OFDInfo {

    private final OFD ofd;

    public OFDInfo(OFD ofd) {
        this.ofd = ofd;
    }

    public OFD getOfd() {
        return ofd;
    }

    /**
     * 验证签名信息是否有效（根据Signature.xml校验）
     * @return true：有效；false：无效
     */
    public boolean verify() {
        for (OFDSignatures.Sign sign : ofd.DocBody.$OFDSignatures.Signature) {
            try {
                // 1. 创建证书对象
                X509Certificate certificate = OfdUtils.parseX509Certificate(sign.$Signature.$CertData);
                // 2. 根证书校验
                if(!OfdUtils.verifyCertificate(certificate)) {
                    return false;
                }
                // 3. 签名校验
                if (!OfdUtils.verifySM2Signature(sign.$Signature.$ToSignData, sign.$Signature.$SignData, certificate)) {
                    return false;
                }
                // 4. 文件的校验
                String checkMethod = sign.$Signature.SignedInfo.References.CheckMethod;
                for (OFDSignatures.Reference reference : sign.$Signature.SignedInfo.References.Reference) {
                    Digest digest;
                    if (checkMethod.equals("1.2.156.10197.1.401")) {
                        digest = new SM3Digest();
                    } else {
                        throw new RuntimeException("不支持的算法");
                    }
                    digest.update(reference.$FileData, 0, reference.$FileData.length);
                    byte[] hash = new byte[digest.getDigestSize()];
                    digest.doFinal(hash, 0);

                    // 比较是否一致
                    String code = Base64Utils.encodeToString(hash);
                    if (!reference.CheckValue.equals(code)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取具体的发票信息（取original_invoice.xml的内容）
     * @return 发票信息
     */
    public Invoice getInvoice() {
        byte[] bytes = ofd.DocBody.$OFDDocument.$OFDAttachments.Attachment.get(0).$FileData;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            Invoice invoice = OfdUtils.xmlToObject(is, Invoice.class);
            invoice.TaxInclusiveTotalAmountCN = BeanUtils.NumberToCN.from(new BigDecimal(invoice.TaxInclusiveTotalAmount));
            return invoice;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文档转成单张图像（长图像，默认150dpi）
     * @return BufferedImage，可以根据需要保存到本地，或显示到其他地方
     */
    public BufferedImage convertAsSingleImage() {
        return convertAsSingleImage(150);
    }

    /**
     * 文档转成单张图像（长图像，指定DPI）
     * @return BufferedImage，可以根据需要保存到本地，或显示到其他地方
     */
    public BufferedImage convertAsSingleImage(int dpi) {
        List<BufferedImage> images = convertAsImages(dpi);
        int width = 0, height = 0;
        for (BufferedImage img : images) {
            width   = Math.max(width, img.getWidth());
            height += img.getHeight();
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // 背景色
        g.setColor(Color.white);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        // 合并图像
        int y = 0;
        for (BufferedImage img : images) {
            // 如果图像小于画布，则居中显示
            int x = (image.getWidth() - img.getWidth()) / 2;
            g.drawImage(img, x, y, null);
            // 设置下次画的起始Y坐标
            y += img.getHeight();
        }
        g.dispose();
        return image;
    }

    /**
     * 文档转成多张图像，一页一张图像（默认150dpi）
     * @return BufferedImage集合，可以根据需要保存到本地，或显示到其他地方
     */
    public List<BufferedImage> convertAsImages() {
        return convertAsImages(150);
    }

    /**
     * 文档转成多张图像，一页一张图像（指定DPI）
     * @return BufferedImage集合，可以根据需要保存到本地，或显示到其他地方
     */
    public List<BufferedImage> convertAsImages(int dpi) {
        List<BufferedImage> pageImageList = new ArrayList<>();
        // 1. 画页面
        for (OFDDocument.Page page : ofd.DocBody.$OFDDocument.Pages.Page) {
            // 画页面
            BufferedImage image = drawPage(ofd, page, dpi, false);
            // 添加到集合中
            pageImageList.add(image);
        }
        // 2. 画签章
        if (!ofd.DocBody.$OFDSignatures.Signature.isEmpty()) {
            for (OFDSignatures.Sign sign : ofd.DocBody.$OFDSignatures.Signature) {
                OFDSignatures.SignedInfo sInfo = sign.$Signature.SignedInfo;
                if (sInfo != null) {
                    OFDDocument.Page sealPage = sInfo.Seal.$OFD.DocBody.$OFDDocument.Pages.Page.get(0);
                    // 画签章
                    BufferedImage sealImg = drawPage(sInfo.Seal.$OFD, sealPage, dpi, true);
                    // 获取章的页码
                    int pageRef = Integer.parseInt(sInfo.StampAnnot.PageRef);
                    // 获取页面对应的图像
                    BufferedImage pageImg = pageImageList.get(pageRef - 1);
                    // 获取画布，并画章
                    Graphics2D g = createGraphics2D(pageImg.getGraphics(), sInfo.StampAnnot.Boundary, null, dpi);
                    g.drawImage(sealImg, 0, 0, null);
                    g.dispose();
                }
            }
        }

        return pageImageList;
    }

    /**
     * 根据每页的描述信息绘制成图像，转成图像的核心方法
     * @param ofd OFD文档对象
     * @param page 需要转成图像的页面
     * @param dpi DPI
     * @param isTransparency 是否透明背景
     * @return 绘制好的图像
     */
    public static BufferedImage drawPage(OFD ofd, OFDDocument.Page page, int dpi, boolean isTransparency) {
        double scale = dpi / 25.4f;
        try {
            // 文档默认区域大小
            String pageArea = ofd.DocBody.$OFDDocument.CommonData.PageArea.PhysicalBox;
            // 计算宽度和高度
            String physicalBox = "";
            if (BeanUtils.isNotEmpty(page.$OFDContent.Area)) {
                physicalBox = page.$OFDContent.Area.PhysicalBox;
            }
            String[] parts = BeanUtils.emptyFilter(physicalBox, pageArea).split(" ");
            int width = (int) (scale * Integer.parseInt(parts[2]));
            int height = (int) (scale * Integer.parseInt(parts[3]));

            // 创建画布
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g;
            if (isTransparency) { // 透明背景
                BufferedImageGraphicsConfig config = BufferedImageGraphicsConfig.getConfig(image);
                image = config.createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);
                g = image.createGraphics();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0.7f)); // 章透明
            } else {
                g = image.createGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
            }

            List<OFDDocument.Page> pages = new ArrayList<>();
            // 模板/页面的线条和数据
            if (page.$OFDContent.Template != null) { // 如果有模板则追加进去
                pages.add((OFDDocument.Page) ofd.ObjectMap.get(page.$OFDContent.Template.TemplateID));
            }
            pages.add(page);

            // 绘图（模板/页面）
            for (OFDDocument.Page pg : pages) {
                // 绘制的层对象
                OFDContent.Layer layer = pg.$OFDContent.Content.Layer;
                // 需要绘制的对象
                List<Object> drawObjects = new ArrayList<>();
                drawObjects.addAll(layer.PathObject);
                drawObjects.addAll(layer.TextObject);
                drawObjects.addAll(layer.ImageObject);
                // 循环绘制
                for (Object drawObj : drawObjects) {
                    if (drawObj instanceof OFDContent.PathObject) {
                        OFDContent.PathObject obj = (OFDContent.PathObject) drawObj;
                        // 创建画布
                        Graphics2D graphics = createGraphics2D(g, obj.Boundary, obj.CTM, dpi);
                        // 画线条
                        if (BeanUtils.isNotEmpty(obj.AbbreviatedData)) {
                            // 设置线条属性
                            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            graphics.setColor(Color.black);
                            if (BeanUtils.isNotEmpty(layer.DrawParam)){
                                OFDDocumentRes.DrawParam drawParam = (OFDDocumentRes.DrawParam) ofd.ObjectMap.get(layer.DrawParam);
                                if (drawParam.StrokeColor != null) {
                                    graphics.setColor(castColor(drawParam.StrokeColor.Value));
                                }
                            }
                            if (obj.StrokeColor != null) {
                                graphics.setColor(castColor(obj.StrokeColor.Value));
                            }
                            if (obj.LineWidth != null) {
                                graphics.setStroke(new BasicStroke((float) (obj.LineWidth * scale)));
                            } else {
                                graphics.setStroke(new BasicStroke((float) (0.353 * scale)));
                            }
                            // 画
                            drawObject(graphics, obj.AbbreviatedData, dpi);
                        }
                        graphics.dispose();
                    } else if (drawObj instanceof OFDContent.TextObject) {
                        OFDContent.TextObject obj = (OFDContent.TextObject) drawObj;
                        // 创建画布
                        Graphics2D graphics = createGraphics2D(g, obj.Boundary, obj.CTM, dpi);
                        // 获取字体
                        OFDDocumentRes.Font font = (OFDDocumentRes.Font) ofd.ObjectMap.get(obj.Font);
                        // 设置文字属性
                        graphics.setFont(new Font(font.FontName, Font.PLAIN, (int) (obj.Size * scale)));
                        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                        graphics.setColor(Color.black);
                        if (BeanUtils.isNotEmpty(layer.DrawParam)){
                            OFDDocumentRes.DrawParam drawParam = (OFDDocumentRes.DrawParam) ofd.ObjectMap.get(layer.DrawParam);
                            if (drawParam.FillColor != null) {
                                graphics.setColor(castColor(drawParam.FillColor.Value));
                            }
                        }
                        if (obj.FillColor != null) {
                            graphics.setColor(castColor(obj.FillColor.Value));
                        }
                        // 画文字
                        for (OFDContent.TextCode textCode : obj.TextCode) {
                            if (BeanUtils.isNotEmpty(textCode.DeltaX) || BeanUtils.isNotEmpty(textCode.DeltaY)) {
                                List<Double> deltaxs = new ArrayList<>();
                                List<Double> deltays = new ArrayList<>();
                                if (BeanUtils.isNotEmpty(textCode.DeltaX)) {
                                    deltaxs = splitDelta(textCode.DeltaX);
                                }
                                if (BeanUtils.isNotEmpty(textCode.DeltaY)) {
                                    deltays = splitDelta(textCode.DeltaY);
                                }
                                float x = 0, y = 0;
                                for (int i = 0; i < textCode.Text.length(); i++) {
                                    String item = textCode.Text.substring(i, i + 1);
                                    graphics.drawString(item, (float) ((textCode.X + x) * scale), (float) ((textCode.Y + y) * scale));
                                    if (i < deltaxs.size()) {
                                        x += deltaxs.get(i);
                                    }
                                    if (i < deltays.size()) {
                                        y += deltays.get(i);
                                    }
                                }
                            } else {
                                graphics.drawString(textCode.Text, (float) (textCode.X * scale), (float) ((0 + textCode.Y) * scale));
                            }
                        }
                        graphics.dispose();
                    } else if (drawObj instanceof OFDContent.ImageObject) {
                        OFDContent.ImageObject obj = (OFDContent.ImageObject) drawObj;
                        // 创建画布
                        Graphics2D graphics = createGraphics2D(g, obj.Boundary, null, dpi);
                        // 画图片
                        OFDDocumentRes.MultiMedia media = (OFDDocumentRes.MultiMedia) ofd.ObjectMap.get(obj.ResourceID);
                        try (InputStream is = new ByteArrayInputStream(media.$FileData)) {
                            BufferedImage mediaImg = OfdUtils.readImageFile(media.Format, is);
                            // 改变大小
                            Rectangle bounds = graphics.getClipBounds();
                            Image mediaImg2 = mediaImg.getScaledInstance(bounds.width, bounds.height, Image.SCALE_SMOOTH);
                            // 画缩小的图
                            graphics.drawImage(mediaImg2, 0, 0, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        graphics.dispose();
                    }
                }
            }
            g.dispose();
            // 返回
            return image;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 矩阵转换
     */
    public static Graphics2D createGraphics2D(Graphics g, String boundary, String ctm, int dpi) {
        double scale = dpi / 25.4f;
        String[] boundStrs = boundary.split(" ");

        Double[] bounds = new Double[boundStrs.length];
        bounds[0] = Double.parseDouble(boundStrs[0]) * scale;
        bounds[1] = Double.parseDouble(boundStrs[1]) * scale;
        bounds[2] = Double.parseDouble(boundStrs[2]) * scale;
        bounds[3] = Double.parseDouble(boundStrs[3]) * scale;

        Graphics2D g2d = (Graphics2D) g.create(
                (int) Math.floor(bounds[0]),
                (int) Math.floor(bounds[1]),
                (int) Math.ceil(bounds[2]),
                (int) Math.ceil(bounds[3]));

        // CTM转换
        if (BeanUtils.isNotEmpty(ctm)) {
            String[] ctms = ctm.split(" ");
            AffineTransform transform = new AffineTransform(
                    Double.parseDouble(ctms[0]),
                    Double.parseDouble(ctms[1]),
                    Double.parseDouble(ctms[2]),
                    Double.parseDouble(ctms[3]),
                    Double.parseDouble(ctms[4]) * scale + bounds[0],
                    Double.parseDouble(ctms[5]) * scale + bounds[1]);
            g2d.setTransform(transform);
        }

        return g2d;
    }

    /**
     * 文字排版数据解析
     * @param delta 排版信息
     * @return 解析后的每个文字的具体值
     */
    public static List<Double> splitDelta(String delta) {
        List<Double> list = new ArrayList<>();
        String[] tmps = delta.trim().split(" ");
        for (int i = 0, len = tmps.length; i < len; i++) {
            if ("g".equals(tmps[i])) {
                for (int j = 0, len2 = Integer.parseInt(tmps[i + 1]); j < len2; j++) {
                    list.add(Double.parseDouble(tmps[i + 2]));
                }
                i += 2;
            } else {
                list.add(Double.parseDouble(tmps[i]));
            }
        }
        return list;
    }

    /**
     * 颜色转换
     */
    public static Color castColor(String color) {
        if (color == null || "".equals(color)) {
            return null;
        }
        String[] colors = color.split(" ");
        int[] colorInts = new int[colors.length];
        for (int i = 0; i< colors.length; i++) {
            colorInts[i] = Integer.parseInt(colors[i]);
        }
        return new Color(colorInts[0], colorInts[1], colorInts[2]);
    }

    /**
     * 形状数据绘制（线、贝塞尔曲线等）
     */
    public static void drawObject(Graphics2D g, String abbreviatedData, int dpi) {
        double scale = dpi / 25.4f;

        GeneralPath path = new GeneralPath();
        String[] parts = abbreviatedData.split(" (?=[SMLQBAC])");
        for (String part : parts) {
            String[] temps = part.split(" ");
            switch (temps[0]) {
                case "S":
                case "M": {
                    double x = (Double.parseDouble(temps[1])) * scale;
                    double y = (Double.parseDouble(temps[2])) * scale;
                    path.moveTo(x, y);
                    break;
                }
                case "L": {
                    double x = (Double.parseDouble(temps[1])) * scale;
                    double y = (Double.parseDouble(temps[2])) * scale;
                    path.lineTo(x, y);
                    break;
                }
                case "Q": {
                    double x1 = (Double.parseDouble(temps[1])) * scale;
                    double y1 = (Double.parseDouble(temps[2])) * scale;
                    double x2 = (Double.parseDouble(temps[3])) * scale;
                    double y2 = (Double.parseDouble(temps[4])) * scale;
                    path.quadTo(x1, y1, x2, y2);
                    break;
                }
                case "B": {
                    double x1 = (Double.parseDouble(temps[1])) * scale;
                    double y1 = (Double.parseDouble(temps[2])) * scale;
                    double x2 = (Double.parseDouble(temps[3])) * scale;
                    double y2 = (Double.parseDouble(temps[4])) * scale;
                    double x3 = (Double.parseDouble(temps[5])) * scale;
                    double y3 = (Double.parseDouble(temps[6])) * scale;
                    path.curveTo(x1, y1, x2, y2, x3, y3);
                    break;
                }
                case "A":
                    // TODO 使用Arc2D画圆弧
                    break;
                case "C": {
                    path.closePath();
                    break;
                }
            }
        }
        g.draw(path);
    }
}
