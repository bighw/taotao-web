package com.taotao.web.bean;

public class Item extends com.taotao.manage.pojo.Item {

    /**
     * 扩展images方便页面使用
     * @return
     */
    public String[] getImages() {
        if (getImage() != null) {
            return getImage().split(",");
        }
        return null;
    }

}
