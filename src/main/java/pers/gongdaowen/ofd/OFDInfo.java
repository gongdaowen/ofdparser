package pers.gongdaowen.ofd;

import pers.gongdaowen.ofd.model.*;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import sun.awt.image.BufferedImageGraphicsConfig;
import pers.gongdaowen.ofd.utils.Base64Utils;
import pers.gongdaowen.ofd.utils.BeanUtils;
import pers.gongdaowen.ofd.utils.OfdUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class OFDInfo {

    private OFD ofd;

    public OFDInfo(OFD ofd) {
        this.ofd = ofd;
    }

    public OFD getOfd() {
        return ofd;
    }

    public boolean verify() {
        for (OFDSignatures.Sign sign : ofd.DocBody.$OFDSignatures.Signature) {
            try {
                // 创建证书对象
                X509Certificate certificate = OfdUtils.parseX509Certificate(sign.$Signature.$CertData);
                // 根证书校验
                if(!OfdUtils.verifyCertificate(certificate)) {
                    return false;
                }
                // 签名校验
                if (!OfdUtils.verifySM2Signature(sign.$Signature.$ToSignData, sign.$Signature.$SignData, certificate)) {
                    return false;
                }
                // 文件的校验
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

    public Invoice getInvoice() {
        byte[] bytes = ofd.DocBody.$OFDDocument.$OFDAttachments.Attachment.get(0).$FileData;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            return OfdUtils.xmlToObject(is, Invoice.class);
        } catch (Exception e) {
            return null;
        }
    }


    public BufferedImage convertAsSingleImage() {
        return convertAsSingleImage(150);
    }

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

    public List<BufferedImage> convertAsImages() {
        return convertAsImages(150);
    }

    public List<BufferedImage> convertAsImages(int dpi) {
        double _dpi = dpi / 25.4f;

        List<BufferedImage> pageImageList = new ArrayList<>();
        // 1. 画页面
        for (OFDDocument.Page page : ofd.DocBody.$OFDDocument.Pages.Page) {
            // 画页面
            BufferedImage image = drawPage(ofd, page, _dpi, false);
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
                    BufferedImage sealImg = this.drawPage(sInfo.Seal.$OFD, sealPage, _dpi, true);
                    // 获取章的页码
                    int pageRef = Integer.valueOf(sInfo.StampAnnot.PageRef);
                    // 获取页面对应的图像
                    BufferedImage pageImg = pageImageList.get(pageRef - 1);
                    // 获取画布，并画章
                    Graphics2D g = this.createGraphics2D(pageImg.getGraphics(), sInfo.StampAnnot.Boundary, null, _dpi);
                    g.drawImage(sealImg, 0, 0, null);
                    g.dispose();
                }
            }
        }

        return pageImageList;
    }

    private BufferedImage drawPage(OFD ofd, OFDDocument.Page page, double dpi, boolean isTransparency) {
        try {
            // 文档默认区域大小
            String pageArea = ofd.DocBody.$OFDDocument.CommonData.PageArea.PhysicalBox;
            // 计算宽度和高度
            String[] parts = BeanUtils.emptyFilter(page.$OFDContent.Area.PhysicalBox, pageArea).split(" ");
            int width = (int) (dpi * Integer.valueOf(parts[2]));
            int height = (int) (dpi * Integer.valueOf(parts[3]));

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
                        Graphics2D graphics = this.createGraphics2D(g, obj.Boundary, obj.CTM, dpi);
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
                                graphics.setStroke(new BasicStroke((float) (obj.LineWidth * dpi)));
                            } else {
                                graphics.setStroke(new BasicStroke((float) (0.353 * dpi)));
                            }
                            // 画
                            this.drawObject(graphics, obj.AbbreviatedData, dpi);
                        }
                        graphics.dispose();
                    } else if (drawObj instanceof OFDContent.TextObject) {
                        OFDContent.TextObject obj = (OFDContent.TextObject) drawObj;
                        // 创建画布
                        Graphics2D graphics = this.createGraphics2D(g, obj.Boundary, obj.CTM, dpi);
                        // 获取字体
                        OFDDocumentRes.Font font = (OFDDocumentRes.Font) ofd.ObjectMap.get(obj.Font);
                        // 设置文字属性
                        graphics.setFont(new Font(font.FontName, Font.PLAIN, (int) (obj.Size * dpi)));
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
                                    graphics.drawString(item, (float) ((textCode.X + x) * dpi), (float) ((textCode.Y + y) * dpi));
                                    if (i < deltaxs.size()) {
                                        x += deltaxs.get(i);
                                    }
                                    if (i < deltays.size()) {
                                        y += deltays.get(i);
                                    }
                                }
                            } else {
                                graphics.drawString(textCode.Text, (float) (textCode.X * dpi), (float) ((0 + textCode.Y) * dpi));
                            }
                        }
                        graphics.dispose();
                    } else if (drawObj instanceof OFDContent.ImageObject) {
                        OFDContent.ImageObject obj = (OFDContent.ImageObject) drawObj;
                        // 创建画布
                        Graphics2D graphics = this.createGraphics2D(g, obj.Boundary, null, dpi);
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

    private Graphics2D createGraphics2D(Graphics g, String boundary, String ctm, double dpi) {
        String[] boundStrs = boundary.split(" ");

        Double[] bounds = new Double[boundStrs.length];
        bounds[0] = Double.valueOf(boundStrs[0]) * dpi;
        bounds[1] = Double.valueOf(boundStrs[1]) * dpi;
        bounds[2] = Double.valueOf(boundStrs[2]) * dpi;
        bounds[3] = Double.valueOf(boundStrs[3]) * dpi;

        Graphics2D g2d = (Graphics2D) g.create(bounds[0].intValue(), bounds[1].intValue(), bounds[2].intValue(), bounds[3].intValue());

        // CTM转换
        if (BeanUtils.isNotEmpty(ctm)) {
            String[] ctms = ctm.split(" ");
            AffineTransform transform = new AffineTransform(
                    Double.valueOf(ctms[0]),
                    Double.valueOf(ctms[1]),
                    Double.valueOf(ctms[2]),
                    Double.valueOf(ctms[3]),
                    Double.valueOf(ctms[4]) * dpi + bounds[0],
                    Double.valueOf(ctms[5]) * dpi + bounds[1]);
            g2d.setTransform(transform);
        }
        return g2d;
    }

    private List<Double> splitDelta(String delta) {
        List<Double> list = new ArrayList<>();
        String[] tmps = delta.trim().split(" ");
        for (int i = 0, len = tmps.length; i < len; i++) {
            if ("g".equals(tmps[i])) {
                for (int j = 0, len2 = Integer.valueOf(tmps[i + 1]); j < len2; j++) {
                    list.add(Double.valueOf(tmps[i + 2]));
                }
                i += 2;
            } else {
                list.add(Double.valueOf(tmps[i]));
            }
        }
        return list;
    }

    private Color castColor(String color) {
        if (color == null || "".equals(color)) {
            return null;
        }
        String[] colors = color.split(" ");
        int[] colorInts = new int[colors.length];
        for (int i = 0; i< colors.length; i++) {
            colorInts[i] = Integer.valueOf(colors[i]);
        }
        return new Color(colorInts[0], colorInts[1], colorInts[2]);
    }

    private void drawObject(Graphics g, String abbreviatedData, double dpi) {
        Point2D.Double sPoint = null, mPoint = null;
        String[] parts = abbreviatedData.split(" (?=[SMLQBAC])");
        for (String part : parts) {
            String[] temps = part.split(" ");
            switch (temps[0]) {
                case "S": {
                    sPoint = new Point2D.Double((Double.valueOf(temps[1])) * dpi, (Double.valueOf(temps[2])) * dpi);
                    break;
                }
                case "M": {
                    mPoint = new Point2D.Double((Double.valueOf(temps[1])) * dpi, (Double.valueOf(temps[2])) * dpi);
                    if (sPoint == null) {
                        sPoint = mPoint;
                    }
                    break;
                }
                case "L": {
                    Point2D.Double lEnd = new Point2D.Double((Double.valueOf(temps[1])) * dpi, (Double.valueOf(temps[2])) * dpi);
                    // 画线
                    g.drawLine((int) mPoint.getX(), (int) mPoint.getY(), (int) lEnd.getX(), (int) lEnd.getY());
                    // 重新设置当前点
                    mPoint = lEnd;
                    break;
                }
                case "Q": {
                    Point.Double[] points = new Point.Double[]{
                            mPoint, //起点
                            new Point.Double((Double.valueOf(temps[1])) * dpi, (Double.valueOf(temps[2])) * dpi), //控制点
                            new Point.Double((Double.valueOf(temps[3])) * dpi, (Double.valueOf(temps[4])) * dpi), //终点
                    };
                    // 贝塞尔曲线
                    drawBezier(g, points);
                    // 重新设置当前点
                    mPoint = points[2];
                    break;
                }
                case "B": {
                    Point.Double[] points = new Point.Double[]{
                            mPoint, //起点
                            new Point.Double((Double.valueOf(temps[1])) * dpi, (Double.valueOf(temps[2])) * dpi), //控制点
                            new Point.Double((Double.valueOf(temps[3])) * dpi, (Double.valueOf(temps[4])) * dpi), //控制点
                            new Point.Double((Double.valueOf(temps[5])) * dpi, (Double.valueOf(temps[6])) * dpi), //终点
                    };
                    // 贝塞尔曲线
                    drawBezier(g, points);
                    // 重新设置当前点
                    mPoint = points[3];
                    break;
                }
                case "A":
                    break;
                case "C": {
                    // 画线
                    g.drawLine((int) mPoint.getX(), (int) mPoint.getY(), (int) sPoint.getX(), (int) sPoint.getY());
                    break;
                }
            }
        }
    }

    private void drawBezier(Graphics g, Point2D.Double[] points) {
        int n = points.length - 1;
        // u的步长决定了曲线点的精度
        for (float u = 0; u <= 1; u += 0.0001) {
            Point.Double[] p = new Point.Double[n + 1];
            for (int i = 0; i <= n; i++) {
                p[i] = new Point.Double(points[i].x, points[i].y);
            }

            for (int r = 1; r <= n; r++) {
                for (int i = 0; i <= n - r; i++) {
                    p[i].x = (1 - u) * p[i].x + u * p[i + 1].x;
                    p[i].y = (1 - u) * p[i].y + u * p[i + 1].y;
                }
            }
            g.drawOval((int) p[0].x, (int) p[0].y, 1, 1);
        }
    }
}
