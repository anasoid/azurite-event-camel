package org.anasoid.azurite.event.routes;

import java.util.Date;

public class EventData {
    private Date date;
    private String method;
    private String account;
    private String container;
    private String file;
    private String subject;
    private String url;
    private int status;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "AccessData{" +
                "date=" + date +
                ", method='" + method + '\'' +
                ", account='" + account + '\'' +
                ", container='" + container + '\'' +
                ", file='" + file + '\'' +
                ", url='" + url + '\'' +
                ", status=" + status +
                '}';
    }
}