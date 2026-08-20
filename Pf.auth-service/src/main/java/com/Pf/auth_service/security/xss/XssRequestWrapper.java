package com.Pf.auth_service.security.xss;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = cleanXSS(values[i]);
        }
        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        if (value == null) {
            return null;
        }
        return cleanXSS(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value == null) {
            return null;
        }
        return cleanXSS(value);
    }
    
    @Override
    public Enumeration<String> getHeaders(String name) {
        Enumeration<String> headers = super.getHeaders(name);
        if (headers == null) {
            return null;
        }
        List<String> cleanHeaders = new ArrayList<>();
        while (headers.hasMoreElements()) {
            cleanHeaders.add(cleanXSS(headers.nextElement()));
        }
        return Collections.enumeration(cleanHeaders);
    }

    private String cleanXSS(String value) {
        if (value != null) {
            // Dùng Jsoup để làm sạch các thẻ HTML độc hại, Safelist.none() chỉ cho phép text thuần túy
            return Jsoup.clean(value, Safelist.none());
        }
        return null;
    }
}
