package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.entity.Banner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页轮播图接口
 */
@RestController
@RequestMapping("/banners")
public class BannerController {

    /**
     * 获取启用的轮播图列表
     * TODO: 后续可改为从数据库读取，目前返回静态配置数据
     */
    @GetMapping
    public Result<List<Banner>> listBanners() {
        List<Banner> banners = new ArrayList<>();

        Banner b1 = new Banner();
        b1.setId(1L);
        b1.setImageUrl("/static/images/banner1.png");
        b1.setTitle("欢迎来到校园服务论坛");
        b1.setSubtitle("发现校园精彩生活");
        b1.setLinkType("page");
        b1.setLinkUrl("/pages/forum/list/list");
        b1.setBgColor("linear-gradient(135deg, #FF6B9D 0%, #A78BFA 100%)");
        b1.setPriority(100);
        b1.setStatus(1);

        Banner b2 = new Banner();
        b2.setId(2L);
        b2.setImageUrl("/static/images/banner2.png");
        b2.setTitle("二手好物等你淘");
        b2.setSubtitle("闲置物品循环利用");
        b2.setLinkType("page");
        b2.setLinkUrl("/pages/service/product/list/list");
        b2.setBgColor("linear-gradient(135deg, #60A5FA 0%, #34D399 100%)");
        b2.setPriority(90);
        b2.setStatus(1);

        Banner b3 = new Banner();
        b3.setId(3L);
        b3.setImageUrl("/static/images/banner1.png");
        b3.setTitle("校园活动火热报名中");
        b3.setSubtitle("参与活动赢取奖励");
        b3.setLinkType("page");
        b3.setLinkUrl("/pages/service/activity/list/list");
        b3.setBgColor("linear-gradient(135deg, #FBBF24 0%, #F87171 100%)");
        b3.setPriority(80);
        b3.setStatus(1);

        Banner b4 = new Banner();
        b4.setId(4L);
        b4.setImageUrl("/static/images/banner2.png");
        b4.setTitle("互助有爱 温暖同行");
        b4.setSubtitle("发布需求 接单帮忙");
        b4.setLinkType("page");
        b4.setLinkUrl("/pages/service/help/list/list");
        b4.setBgColor("linear-gradient(135deg, #A78BFA 0%, #EC4899 100%)");
        b4.setPriority(70);
        b4.setStatus(1);

        banners.add(b1);
        banners.add(b2);
        banners.add(b3);
        banners.add(b4);

        return Result.success(banners);
    }
}
