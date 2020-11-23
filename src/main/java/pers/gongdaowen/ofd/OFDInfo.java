package pers.gongdaowen.ofd;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import pers.gongdaowen.ofd.model.Invoice;
import pers.gongdaowen.ofd.model.OFD;
import pers.gongdaowen.ofd.model.OFDSignatures;
import pers.gongdaowen.ofd.utils.Base64Utils;
import pers.gongdaowen.ofd.utils.BeanUtils;
import pers.gongdaowen.ofd.utils.OfdUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.cert.X509Certificate;

/**
 * OFD信息处理类
 */
public class OFDInfo {

    private final OFD ofd;
    private final OFDPainter ofdPainter;

    public OFDInfo(OFD ofd) {
        this.ofd = ofd;
        this.ofdPainter = new OFDPainter(ofd);
    }

    public OFD getOfd() {
        return ofd;
    }

    public OFDPainter getOfdPainter() {
        return ofdPainter;
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
}
