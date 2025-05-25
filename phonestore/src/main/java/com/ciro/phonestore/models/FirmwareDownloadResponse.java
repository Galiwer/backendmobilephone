package com.ciro.phonestore.models;

public class FirmwareDownloadResponse {
    private String type;
    private String url;
    private String fileName;
    private String contentType;
    private String contentDisposition;
    private String message;

    public FirmwareDownloadResponse() {
    }

    public static FirmwareDownloadResponse forLink(Firmware firmware) {
        FirmwareDownloadResponse response = new FirmwareDownloadResponse();
        response.setType("link");
        response.setUrl(firmware.getFirmwareLink());
        response.setFileName(String.format("%s_%s_firmware.bin", firmware.getBrand(), firmware.getModel()));
        return response;
    }

    public static FirmwareDownloadResponse forFile(Firmware firmware, String downloadUrl) {
        FirmwareDownloadResponse response = new FirmwareDownloadResponse();
        response.setType("file");
        response.setUrl(downloadUrl);
        response.setFileName(firmware.getFileName());
        response.setContentType("application/octet-stream");
        response.setContentDisposition("attachment; filename=\"" + firmware.getFileName() + "\"");
        return response;
    }

    public static FirmwareDownloadResponse error(String message) {
        FirmwareDownloadResponse response = new FirmwareDownloadResponse();
        response.setType("error");
        response.setMessage(message);
        return response;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}