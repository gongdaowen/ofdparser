package pers.gongdaowen.ofd;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import pers.gongdaowen.ofd.model.*;
import pers.gongdaowen.ofd.utils.BeanUtils;
import pers.gongdaowen.ofd.utils.OfdUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * OFD绘图类，将OFD文件转换成图像
 */
public class OFDPainter {

    private final OFD ofd;
    private PageHandler pageHandler = PageHandler.DEFAULT_HANDLER;

    public OFDPainter(OFD ofd) {
        this(ofd, null);
    }

    public OFDPainter(OFD ofd, PageHandler pageHandler) {
        this.ofd = ofd;
        if (pageHandler != null) {
            this.pageHandler = pageHandler;
        }
    }

    public void setPageHandler(PageHandler pageHandler) {
        this.pageHandler = pageHandler;
    }

    /**
     * 文档转成单张图像（长图像，指定DPI）
     * @return BufferedImage，可以根据需要保存到本地，或显示到其他地方
     */
    public BufferedImage convertAsSingleImage(int dpi) {
        List<BufferedImage> images = this.convertAsImages(dpi);
        return BeanUtils.mergeImages(images);
    }

    /**
     * 文档转成多张图像，一页一张图像（指定DPI）
     * @return BufferedImage集合，可以根据需要保存到本地，或显示到其他地方
     */
    public List<BufferedImage> convertAsImages(int dpi) {

        List<BufferedImage> pageImageList = new ArrayList<>();

        // 循环处理每个页面
        for (OFDDocument.Page page : ofd.DocBody.$OFDDocument.Pages.Page) {

            double scale = dpi / 25.4f;
            // 获取页面宽度和高度
            int width  = (int) (scale * page.$Rectangle.getWidth());
            int height = (int) (scale * page.$Rectangle.getHeight());

            // 构造画布
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());

            // 画页面内容
            this.drawPage(g, page, dpi);

            // 添加到集合中
            pageImageList.add(image);
        }

        return pageImageList;
    }

    /**
     * 文档转成单个SVG（长图像，指定DPI）
     * @return SVGGraphics2D，可以根据需要保存到本地，或显示到其他地方
     */
    public SVGGraphics2D convertAsSingleSVG(int dpi) {

        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGraphics2D pageSvg = new SVGGraphics2D(document);
        int width = 0, height = 0;

        // 循环处理每个页面
        for (OFDDocument.Page page : ofd.DocBody.$OFDDocument.Pages.Page) {

            // 获取页面宽度和高度
            double scale = dpi / 25.4f;
            int width0  = (int) (scale * page.$Rectangle.getWidth());
            int height0 = (int) (scale * page.$Rectangle.getHeight());
            width = Math.max(width, width0);
            height += height0;

            // 画页面内容
            drawPage(pageSvg, page, dpi);

            // 平移到下一页位置
            pageSvg.translate(0, height0);
        }

        // 画布总大小
        pageSvg.setSVGCanvasSize(new Dimension(width, height));

        return pageSvg;
    }

    /**
     * 文档转成多个SVG，一页一个SVG（指定DPI）
     * @return SVGGraphics2D集合，可以根据需要保存到本地，或显示到其他地方
     */
    public List<SVGGraphics2D> convertAsSVGs(int dpi) {

        List<SVGGraphics2D> pageSvgList = new ArrayList<>();

        // 循环处理每个页面
        for (OFDDocument.Page page : ofd.DocBody.$OFDDocument.Pages.Page) {

            // 获取页面宽度和高度
            double scale = dpi / 25.4f;
            int width  = (int) (scale * page.$Rectangle.getWidth());
            int height = (int) (scale * page.$Rectangle.getHeight());

            // 构造画布
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
            SVGGraphics2D pageSvg = new SVGGraphics2D(document);
            pageSvg.setSVGCanvasSize(new Dimension(width, height));
            // 画页面内容
            drawPage(pageSvg, page, dpi);

            // 添加到集合中
            pageSvgList.add(pageSvg);
        }

        return pageSvgList;
    }

    /**
     * 根据每页的描述信息绘制成图像，转成图像的核心方法
     * @param g 画布
     * @param page 需要转成图像的页面
     * @param dpi DPI
     */
    public void drawPage(Graphics2D g, OFDDocument.Page page, int dpi) {
        // 页面绘制开始处理
        this.pageHandler.before(g, ofd, page, dpi);

        double scale = dpi / 25.4f;
        try {
            List<OFDDocument.Page> pages = new ArrayList<>();
            // 模板/页面的线条和数据
            if (page.$OFDContent.Template != null) { // 如果有模板则追加进去
                pages.add((OFDDocument.Page) ofd.ObjectMap.get(page.$OFDContent.Template.TemplateID));
            }
            pages.add(page);

            // 检查当前页面是否有章
            List<OFDSignatures.Sign> sealList = ofd.PageSignMap.get(page.$Index);
            if (BeanUtils.isNotEmpty(sealList)) {
                // 有章，循环画章
                for (OFDSignatures.Sign sInfo : sealList) {
                    OFDSignatures.SignedInfo signedInfo = sInfo.$Signature.SignedInfo;
                    // 章的OFD对象
                    OFD sealOFD = signedInfo.Seal.$OFD;
                    OFDPainter sealPainter = new OFDPainter(sealOFD);
                    // 画章的范围
                    Graphics2D sg = createGraphics2D(g, signedInfo.StampAnnot.Boundary, null, dpi);
                    // 画OFD文件（循环画每个页面）
                    for (OFDDocument.Page sPage : sealOFD.DocBody.$OFDDocument.Pages.Page) {
                        // 画页面
                        sealPainter.drawPage(sg, sPage, dpi);
                    }
                }
            }

            // 透明处理
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));

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

            // 页面绘制开始处理
            this.pageHandler.after(g, ofd, page, dpi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    /**
     * 根据区域及矩阵参数创建画布
     */
    public static Graphics2D createGraphics2D(Graphics2D g, String boundary, String ctm, int dpi) {
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
        g2d.translate(bounds[0] - Math.floor(bounds[0]), bounds[1] - Math.floor(bounds[1]));

        // CTM转换
        if (BeanUtils.isNotEmpty(ctm)) {
            String[] ctms = ctm.split(" ");
            AffineTransform transform = g2d.getTransform();
            AffineTransform newTransform = new AffineTransform(
                    Double.parseDouble(ctms[0]),
                    Double.parseDouble(ctms[1]),
                    Double.parseDouble(ctms[2]),
                    Double.parseDouble(ctms[3]),
                    Double.parseDouble(ctms[4]) * scale,
                    Double.parseDouble(ctms[5]) * scale);
            transform.concatenate(newTransform);
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
        if (BeanUtils.isEmpty(color)) {
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
     * 页面绘制处理程序
     * @remark 页面获取方式：pageIndex = ofd.DocBody.$OFDDocument.Pages.Page.indexOf(page) + 1
     */
    public static interface PageHandler {
        void before(Graphics2D g, OFD ofd, OFDDocument.Page page, int dpi);
        void after(Graphics2D g, OFD ofd, OFDDocument.Page page, int dpi);

        static PageHandler DEFAULT_HANDLER = new PageHandler() {
            public void before(Graphics2D g, OFD ofd, OFDDocument.Page page, int dpi) {
            }
            public void after(Graphics2D g, OFD ofd, OFDDocument.Page page, int dpi) {
            }
        };
    }
}
