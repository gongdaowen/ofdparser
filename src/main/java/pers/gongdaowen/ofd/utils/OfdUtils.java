package pers.gongdaowen.ofd.utils;

import org.apache.pdfbox.jbig2.JBIG2ImageReader;
import org.apache.pdfbox.jbig2.JBIG2ImageReaderSpi;
import org.apache.pdfbox.jbig2.io.DefaultInputStreamFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class OfdUtils {

    // X509证书工厂
    private static CertificateFactory bcX509Factory;
    // 根证书
    private static List<X509Certificate> rootCerts = new ArrayList<>();

    static {
        try {
            // 添加BouncyCastleProvider
            Security.addProvider(new BouncyCastleProvider());
            // 创建X509证书工厂
            bcX509Factory = CertificateFactory.getInstance("X.509", "BC");
            String[] certs = {
                    "MIIDfDCCAyCgAwIBAgIGAwAAAAAtMAwGCCqBHM9VAYN1BQAwVTELMAkGA1UEBhMCQ04xGzAZBgNVBAsMEuWbveWutueojuWKoeaAu+WxgDEpMCcGA1UEAwwg56iO5Yqh55S15a2Q6K+B5Lmm5LiA57qn5qC5KFNNMikwHhcNMTQwMTAxMDAwMDAwWhcNMzQwMTAxMDAwMDAwWjBYMQswCQYDVQQGEwJDTjEbMBkGA1UECwwS5Zu95a6256iO5Yqh5oC75bGAMSwwKgYDVQQDDCPnqI7liqHnlLXlrZDor4HkuabnrqHnkIbkuK3lv4MoU00yKTBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABEfs2VQb4RNnX2J7WV+K58GyI+NxejPTSZgvPoiTst7vojR1WiUdcEkoxxBm5QucyAlXga5BrDrBeaCZMk2yEWWjggHVMIIB0TARBglghkgBhvhCAQEEBAMCAQYwDgYDVR0PAQH/BAQDAgEGMBIGA1UdEwEB/wQIMAYBAf8CAQEwHwYDVR0jBBgwFoAUDAQKwUHmQA8RNxHQ6h+TBsOgIrUwHQYDVR0OBBYEFLiwmmAH6gW3sLfny0J2MSmXFXazMBMGCSsGAQQBgjcUAgQGHgQAQwBBMEQGCSqGSIb3DQEJDwQ3MDUwDgYIKoZIhvcNAwICAgCAMA4GCCqGSIb3DQMEAgIAgDAHBgUrDgMCBzAKBggqhkiG9w0DBzB+BgNVHR8EdzB1MHOgcaBvhm1sZGFwOi8vdGF4aWEuY2hpbmF0YXguZ292LmNuOjIzODkvY249Y2FjcmwwMyxvdT1jYWNybCxvdT1jcmwsYz1jbj9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0LCo/YmFzZT9jbj1jYWNybDAzMBoGCisGAQQBqUNkBQYEDAwKQzAwMDAwMDAwMTAaBgorBgEEAalDZAUJBAwMCkMwMDAwMDAwMDEwEQYKKwYBBAGpQ2QCAQQDDAEzMBIGCisGAQQBqUNkAgQEBAwCQ0EwHgYIYIZIAYb4QwkEEgwQMDAwMDAwMDAwMDAwMDAwODAMBggqgRzPVQGDdQUAA0gAMEUCIQCyMS9//FiZaob3P6vw27uSrNmavTWf7mZX6nz++RgQPgIgacgFB0dACbLWFPQ7RGKpgDbppghngcKgxZ2LSxMPJjw=",
                    "MIIC7DCCApGgAwIBAgIGAwAAAAAfMAwGCCqBHM9VAYN1BQAwVTELMAkGA1UEBhMCQ04xGzAZBgNVBAsMEuWbveWutueojuWKoeaAu+WxgDEpMCcGA1UEAwwg56iO5Yqh55S15a2Q6K+B5Lmm5LiA57qn5qC5KFNNMikwHhcNMTQwMTAxMDAwMDAwWhcNMzQwMTAxMDAwMDAwWjBVMQswCQYDVQQGEwJDTjEbMBkGA1UECwwS5Zu95a6256iO5Yqh5oC75bGAMSkwJwYDVQQDDCDnqI7liqHnlLXlrZDor4HkuabkuIDnuqfmoLkoU00yKTBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABOdxU7H/9vfcc24PSJ1Ypi6/gCCqvmb1axOl0oGv1U/3rGXnd/L3nDG01WwhU8rt9MaSsG4uHMVQO68Xf4b9AsujggFJMIIBRTARBglghkgBhvhCAQEEBAMCAQYwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wHwYDVR0jBBgwFoAUDAQKwUHmQA8RNxHQ6h+TBsOgIrUwHQYDVR0OBBYEFAwECsFB5kAPETcR0OofkwbDoCK1MBMGCSsGAQQBgjcUAgQGHgQAQwBBMEQGCSqGSIb3DQEJDwQ3MDUwDgYIKoZIhvcNAwICAgCAMA4GCCqGSIb3DQMEAgIAgDAHBgUrDgMCBzAKBggqhkiG9w0DBzAVBgorBgEEAalDZAUGBAcMBVMxMDEzMBUGCisGAQQBqUNkBQkEBwwFUzEwMTMwEgYKKwYBBAGpQ2QCAQQEDAIwMTASBgorBgEEAalDZAIEBAQMAkNBMB4GCGCGSAGG+EMJBBIMEDAwMDAwMDAwMDAwMDAwMDEwDAYIKoEcz1UBg3UFAANHADBEAiBDYJfgPDjY2S/NiMWojjdMRN7WxGvgkIz/oya927BLNQIgQmCJhGrOP58/sRx7jc0CWVVyw4j9w6Kwq1lWE3USEIY=",
                    "MIICvDCCAmGgAwIBAgIDEAAAMAwGCCqBHM9VAYN1BQAwajELMAkGA1UEBhMCQ04xEDAOBgNVBAgMB0JlaUppbmcxEDAOBgNVBAcMB0JlaUppbmcxETAPBgNVBAoMCFNlY3VyaXR5MREwDwYDVQQLDAhTZWFsUm9vdDERMA8GA1UEAwwIU2VhbFJvb3QwHhcNMTkxMDEwMDQzMjQ4WhcNMjkxMDA3MDQzMjQ4WjA8MQswCQYDVQQGEwJDTjEPMA0GA1UELQwGQTAwMTAxMRwwGgYDVQQDDBPlm73lrrbnqI7liqHmgLvlsYAAMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEOs4LoKmyRfZA0peDoAZdiaZa7mCx6mznbRjm9SN17EZHA/TDw/X0zjpFk9ce64kdLERcy1abTqwMRQOI3z5/OqOCASAwggEcMB0GA1UdDgQWBBQyuTCiweowcP7Vyyy41Mz99iYZLzASBgNVHRMBAf8ECDAGAQH/AgEAMIGbBgNVHSMEgZMwgZCAFNo5o+5ea0sNMlW/75VgGJCv2AcJoW6kbDBqMQswCQYDVQQGEwJDTjEQMA4GA1UECAwHQmVpSmluZzEQMA4GA1UEBwwHQmVpSmluZzERMA8GA1UECgwIU2VjdXJpdHkxETAPBgNVBAsMCFNlYWxSb290MREwDwYDVQQDDAhTZWFsUm9vdIIIESIzRFVmd4gwDgYDVR0PAQH/BAQDAgEGMDkGA1UdHwQyMDAwLqAsoCqGKGh0dHA6Ly93d3cudGVzdC5jb20uY24vdGVzdDRjZXJ0L2FsbC5jcmwwDAYIKoEcz1UBg3UFAANHADBEAiBCwOJ/nbptksG5PpJmR9KALj/2G7OEI1mwuXjciYmJmAIgaBALdqxe9zJ5hoJ6oJNjiuyhBx0vrbD2GY1BJlAnF+k=",
                    "MIICcDCCAhWgAwIBAgIIESIzRFVmd4gwDAYIKoEcz1UBg3UFADBqMQswCQYDVQQGEwJDTjEQMA4GA1UECAwHQmVpSmluZzEQMA4GA1UEBwwHQmVpSmluZzERMA8GA1UECgwIU2VjdXJpdHkxETAPBgNVBAsMCFNlYWxSb290MREwDwYDVQQDDAhTZWFsUm9vdDAeFw0xOTEwMTAwNDMyNDNaFw0yOTEwMDcwNDMyNDNaMGoxCzAJBgNVBAYTAkNOMRAwDgYDVQQIDAdCZWlKaW5nMRAwDgYDVQQHDAdCZWlKaW5nMREwDwYDVQQKDAhTZWN1cml0eTERMA8GA1UECwwIU2VhbFJvb3QxETAPBgNVBAMMCFNlYWxSb290MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEIbWkWPYe2OV2yCkORmaR7RoKD9Icb22dsrV2jXDUR4E8eibh5bXmHbr8FTOTurEv/+y0BiEku0wNV5hrvxKK9qOBojCBnzASBgNVHRMBAf8ECDAGAQH/AgEBMB0GA1UdDgQWBBTaOaPuXmtLDTJVv++VYBiQr9gHCTAfBgNVHSMEGDAWgBTaOaPuXmtLDTJVv++VYBiQr9gHCTAOBgNVHQ8BAf8EBAMCAQYwOQYDVR0fBDIwMDAuoCygKoYoaHR0cDovL3d3dy50ZXN0LmNvbS5jbi90ZXN0NGNlcnQvYWxsLmNybDAMBggqgRzPVQGDdQUAA0cAMEQCIGqvoraDiJt+NGE5yM6VDEU9fWR19EZIulY2eL4F/C52AiBvjEbDFfuC1a0MZxdWXwDzp8ndKWbMxLL3lbHxdDzZ1A=="
            };
            for (String certStr : certs) {
                rootCerts.add(parseX509Certificate(Base64Utils.decodeFromString(certStr)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static X509Certificate parseX509Certificate(byte[] bytes) throws CertificateException {
        return (X509Certificate) bcX509Factory.generateCertificate(new ByteArrayInputStream(bytes));
    }

    public static boolean verifySM2Signature(byte[] toSignData, byte[] signData, X509Certificate cert) {
        try {
            Signature signature = Signature.getInstance("1.2.156.10197.1.501", "BC");
            signature.initVerify(cert.getPublicKey());
            signature.update(toSignData);
            return signature.verify(signData);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean verifyCertificate(X509Certificate cert) {
        try {
            // 有效期校验
            cert.checkValidity();
            // 根证书校验
            for (X509Certificate rootCert : rootCerts) {
                try {
                    cert.verify(rootCert.getPublicKey());
                    return true;
                } catch (Exception e) {
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static Document getDocument(ZipFile zipFile, String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        try (InputStream is = zipFile.getInputStream(zipFile.getEntry(filePath))) {
            return new SAXReader().read(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getInputStream(ZipFile zipFile, String filePath) throws IOException {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        return zipFile.getInputStream(zipFile.getEntry(filePath));
    }

    public static <T> T xmlToObject(File file, Class<T> clz) throws Exception {
        try (InputStream is = new FileInputStream(file)) {
            return xmlToObject(is, clz);
        }
    }

    public static <T> T xmlToObject(InputStream is, Class<T> clz) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(clz);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (T) unmarshaller.unmarshal(is);
    }

    public static String xmlFromObject(Object obj) throws Exception {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            xmlFromObject(obj, os);
            return os.toString();
        }
    }

    public static void xmlFromObject(Object obj, OutputStream os) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(obj.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(obj, os);
    }

    public static BufferedImage readImageFile(String format, InputStream image) throws IOException {
        if ("JB2".equalsIgnoreCase(format)) {
            DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
            ImageInputStream imageInputStream = disf.getInputStream(image);
            JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
            imageReader.setInput(imageInputStream);
            return imageReader.read(0, imageReader.getDefaultReadParam());
        } else {
            return ImageIO.read(image);
        }
    }
}
