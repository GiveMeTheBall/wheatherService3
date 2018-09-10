package com.hust.grid.email;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.hust.grid.bean.WeatherInfo;
import com.hust.grid.cache.ConstantCacheCenter;
import com.hust.grid.weather.WeatherCrawler;
import com.sun.mail.util.MailSSLSocketFactory;

public class MailSender {
    private WeatherInfo weatherInfo;
    private static Properties prop = new Properties();
    private ConstantCacheCenter constantCacheCenter = ConstantCacheCenter.getInstance();

    private static class MyAuthenticator extends Authenticator {
        private String username;
        private String password;

        public MyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

    public MailSender(WeatherInfo weatherInfo) {
        this.weatherInfo = weatherInfo;
    }

    public void sendToAll() {
        List<String> receivers = constantCacheCenter.getReceivers();
        for (String receiver : receivers) {
            send(receiver);
        }
    }

    private void send(String receiver) {
        prop.setProperty("mail.transport.protocol", constantCacheCenter.getProtocol());
        prop.setProperty("mail.smtp.host", constantCacheCenter.getHost());
        prop.setProperty("mail.smtp.port", constantCacheCenter.getPort());
        prop.setProperty("mail.smtp.auth", "true");
        MailSSLSocketFactory mailSSLSocketFactory = null;
        try {
            mailSSLSocketFactory = new MailSSLSocketFactory();
            mailSSLSocketFactory.setTrustAllHosts(true);
        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", mailSSLSocketFactory);
        //
        Session session = Session.getDefaultInstance(prop, new MyAuthenticator(constantCacheCenter.getUsername(), constantCacheCenter.getAuthorizationCode()));
        session.setDebug(true);
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(constantCacheCenter.getSenderEmail(), constantCacheCenter.getSenderName()));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
            mimeMessage.setSubject("明日天气");
            mimeMessage.setSentDate(new Date());

            mimeMessage.setText("Hi, Dear: \n\n     明天天气状态如下：" + weatherInfo.toString());
            mimeMessage.saveChanges();
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
    	 WeatherCrawler crawlWeatherInfo = new WeatherCrawler();
    	 WeatherInfo weatherInfo =  crawlWeatherInfo.crawlWeather("http://www.weather.com.cn/weather/101110101.shtml");
//        List<String> receivers = new ArrayList<String>();
//        receivers.add("328762413@qq.com");
        ConstantCacheCenter constantCacheCenter = ConstantCacheCenter.getInstance();
        constantCacheCenter.init("E:\\weather-service-master\\target\\weather-service\\conf");
        MailSender s = new MailSender(weatherInfo);
        s.sendToAll();
    }
}