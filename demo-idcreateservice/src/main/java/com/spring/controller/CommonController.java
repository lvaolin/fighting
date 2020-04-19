package com.spring.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;


/**
 * 通用服务
 * @author lvaolin
 * @create 2019/6/12 11:13 AM
 */
@RestController
@Component
@RequestMapping("/common/facade")
public class CommonController {



    /**
     * 获取验证码服务  1、有效期到期自动生效 2、只能验证一次 3、验证码存入redis  4、登录时从redis获取验证
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/captcha/image")
    public void getCaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
       /* HttpSession session = request.getSession();
        //集群模式从 redis获取
        String code = (String)session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        //log.info("旧验证码: " + code );

        response.setDateHeader("Expires", 0);

        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");

        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");

        // return a jpeg
        response.setContentType("image/jpeg");

        // create the text for the image
        String capText = captchaProducer.createText();
        //log.info("新验证码："+capText);
        // store the text in the session   集群模式下存入 redis
        session.setAttribute(Constants.KAPTCHA_SESSION_KEY, capText);

        // create the image with the text
        BufferedImage bi = captchaProducer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();

        // write the data out
        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }*/
        return ;
    }




}