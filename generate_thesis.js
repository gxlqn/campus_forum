const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Header, Footer, AlignmentType, PageOrientation, LevelFormat,
  HeadingLevel, BorderStyle, WidthType, ShadingType,
  VerticalAlign, PageNumber, PageBreak, TableOfContents,
  ImageRun
} = require('docx');
const fs = require('fs');

// ==================== 论文内容定义 ====================

const THESIS_TITLE = "基于微信小程序的校园服务论坛系统的设计与实现";

const ABSTRACT_CN = `随着移动互联网技术的快速发展和智能手机的普及，校园信息化建设已成为高校发展的重要方向。传统的校园信息发布方式存在信息分散、交互性差、时效性低等问题，难以满足当代大学生对高效、便捷校园服务的需求。针对这一现状，本文设计并实现了一套基于微信小程序的校园服务论坛系统，旨在为高校师生提供一个集信息交流、资源共享、生活服务于一体的综合性平台。

本系统采用前后端分离的三层架构模式进行设计与开发。前端基于微信小程序框架（WXML/WXSS/JavaScript）构建用户端应用，利用微信生态的天然优势实现了无需下载安装即可使用的轻量级访问体验；后端采用Spring Boot 2.7.18框架结合Java 17语言开发RESTful API服务接口，使用MyBatis-Plus作为持久层ORM框架与MySQL 8.0数据库进行数据交互。系统安全方面，集成了Spring Security框架实现基于JWT（JSON Web Token）的无状态身份认证机制和RBAC（Role-Based Access Control）权限控制模型，确保了用户数据的安全性和系统操作的规范性。

在功能模块设计上，本系统涵盖了九大核心业务领域：用户管理模块支持多角色身份认证与个人信息维护；论坛帖子模块提供分类浏览、发帖回复及点赞收藏功能；二手交易模块实现了商品发布、在线沟通及交易状态跟踪；失物招领模块帮助用户快速发布和查找遗失物品信息；校园活动模块支持活动创建、报名参与及签到管理；互助跑腿模块引入了任务发布、抢单接单及结算支付流程的完整闭环；消息通知模块实现了站内消息推送和系统公告功能；信息聚合模块通过轮播图和推荐算法整合各类校园资源；内容审核模块内置了基于DFA（Deterministic Finite Automaton）算法的敏感词过滤引擎，有效保障了社区内容的健康合规。

经过系统测试验证，各核心功能模块均能正常运行，响应时间控制在合理范围内，系统具有良好的稳定性、可扩展性和用户体验效果。本系统的成功研发不仅为校园师生提供了便捷实用的信息服务工具，也为同类校园信息化平台的开发提供了可参考的技术方案和实践经验。`;

const ABSTRACT_EN = `With the rapid development of mobile Internet technology and the widespread adoption of smartphones, campus information construction has become a crucial direction for university development. Traditional methods of campus information dissemination suffer from problems such as scattered information, poor interactivity, and low timeliness, which fail to meet the needs of contemporary college students for efficient and convenient campus services. To address this situation, this paper designs and implements a campus service forum system based on WeChat Mini Program, aiming to provide university teachers and students with a comprehensive platform integrating information exchange, resource sharing, and life services.

This system adopts a three-tier architecture pattern with front-end and back-end separation for design and development. The front-end is built using the WeChat Mini Program framework (WXML/WXSS/JavaScript) to construct the user application, leveraging the natural advantages of the WeChat ecosystem to achieve a lightweight access experience without requiring downloads or installations. The back-end employs the Spring Boot 2.7.18 framework combined with Java 17 language to develop RESTful API service interfaces, using MyBatis-Plus as the persistence layer ORM framework to interact with MySQL 8.0 database. In terms of system security, the Spring Security framework is integrated to implement stateless authentication mechanisms based on JWT (JSON Web Token) and RBAC (Role-Based Access Control) permission control models, ensuring user data security and standardization of system operations.

In functional module design, this system covers nine core business areas: the user management module supports multi-role identity authentication and personal information maintenance; the forum post module provides categorized browsing, posting/replying, and like/favorite functions; the second-hand trading module implements product publishing, online communication, and transaction status tracking; the lost-and-found module helps users quickly publish and search for lost item information; the campus activity module supports activity creation, registration participation, and check-in management; the mutual assistance errand module introduces a complete closed-loop of task publishing, order grabbing, and settlement payment processes; the message notification module implements in-site message push and announcement functions; the information aggregation module integrates various campus resources through carousels and recommendation algorithms; the content audit module incorporates a sensitive word filtering engine based on DFA (Deterministic Finite Automaton) algorithm to effectively ensure the health and compliance of community content.

Through system testing verification, all core functional modules operate normally, response times are controlled within reasonable ranges, and the system demonstrates good stability, scalability, and user experience effects. The successful research and development of this system not only provides convenient and practical information service tools for campus teachers and students but also offers referenceable technical solutions and practical experience for the development of similar campus informatization platforms.`;

const KEYWORDS = ["微信小程序", "校园服务平台", "Spring Boot", "论坛系统", "JWT认证", "DFA敏感词过滤"];
const KEYWORDS_EN = ["WeChat Mini Program", "Campus Service Platform", "Spring Boot", "Forum System", "JWT Authentication", "DFA Sensitive Word Filtering"];

// ==================== 章节内容 ====================

const CHAPTER1 = {
  sections: [
    {
      title: "1.1 研究背景与意义",
      paragraphs: [
        `近年来，移动互联网技术取得了突飞猛进的发展，智能手机已全面普及并深度融入人们的日常生活。根据中国互联网络信息中心（CNNIC）发布的统计报告显示，截至2024年底，我国手机网民规模已达10.8亿人，其中学生群体占比超过25%，是互联网使用最为活跃的人群之一。与此同时，微信作为国内最大的即时通讯平台，月活跃用户数突破13亿，其内置的小程序生态系统凭借"用完即走"、无需下载安装的特性，已成为移动端应用开发的重要选择。`,
        `高校作为知识密集型组织，每天都会产生大量的信息交互需求：学术讲座通知、社团活动招募、物品转让交易、失物招领启事、学习资料共享、生活互助服务等。然而，当前大多数高校的信息发布渠道仍然较为分散——教务系统、校园官网、QQ群、微信群、贴吧等各自为政，导致信息获取效率低下、传播覆盖面有限、互动体验不佳等问题普遍存在。`,
        `在此背景下，设计和开发一款基于微信小程序的综合性校园服务论坛系统具有重要的现实意义：`,
        `从实用价值角度而言，该系统能够将分散的校园信息资源进行有机整合，通过统一的移动端入口为师生提供便捷的一站式服务，显著提升信息获取效率和校园生活质量。`,
        `从技术实践角度而言，本项目涵盖了前端小程序开发、后端微服务架构、数据库设计优化、安全认证机制、内容过滤算法等多个技术领域的综合运用，对于计算机专业学生的工程能力培养具有很好的锻炼作用。`,
        `从推广应用角度而言，基于微信生态的小程序天然具备社交传播属性，有利于产品在高校群体中的低成本推广和快速普及，具有较好的社会效益和应用前景。`
      ]
    },
    {
      title: "1.2 国内外研究现状",
      paragraphs: [
        `在国外，校园信息化平台的建设起步较早且相对成熟。以美国的Blackboard Learn、Canvas LMS为代表的学习管理系统已经广泛应用于全球数万所教育机构，它们提供课程管理、在线作业、成绩分析等功能，但主要聚焦于教学辅助领域而非泛化的校园生活服务。英国的StudentCrowd、美国的Rate My Professors等平台则专注于特定场景的信息分享，如课程评价、校园生活指南等，但缺乏对本地化服务的深入支持。此外，Facebook Groups、Reddit等通用社交媒体也被部分国外高校用于非官方的信息交流渠道，但由于缺乏针对性设计，用户体验和安全性难以得到保障。`,
        `在国内，随着"互联网+教育"战略的深入推进，各类校园类应用如雨后春笋般涌现。超级校园、今日校园等商业化产品试图打造一站式校园服务平台，但往往面临推广门槛高、定制灵活性不足的问题。各大高校自主开发的官方APP或公众号虽然能够满足基本需求，但受限于开发资源和维护成本，功能通常较为单一且更新迭代缓慢。`,
        `在技术架构层面，国内外相关研究呈现出以下发展趋势：（1）前后端分离架构成为主流选择，便于多端适配和维护升级；（2）微服务思想逐渐渗透至中小型项目，提升系统的可扩展性；（3）人工智能技术开始应用于内容审核、智能推荐等场景；（4）实时通信技术在即时消息、协同编辑等领域得到广泛应用。（5）容器化和云原生部署方式降低了运维复杂度。`,
        `综上所述，虽然市场上已存在多种校园服务类产品，但真正能够兼顾功能完整性、技术先进性、部署便捷性和使用体验性的解决方案仍然稀缺。本研究正是在此背景下展开，力求填补这一空白。`
      ]
    },
    {
      title: "1.3 研究内容与目标",
      paragraphs: [
        `本论文的主要研究内容包括以下几个方面：`,
        `第一，需求分析与系统规划。通过对目标用户群体的调研访谈，梳理出校园服务场景下的核心业务需求和功能优先级，形成完整的需求规格说明书，作为后续设计的依据。`,
        `第二，总体架构与技术选型。基于需求分析结果，确定系统的整体技术路线，包括前后端框架选型、数据库方案、安全策略、部署架构等关键决策。`,
        `第三，数据库设计与实现。根据实体关系模型设计合理的数据库表结构，建立表间关联关系，编写建表SQL脚本，并进行规范化处理以保证数据一致性和查询效率。`,
        `第四，核心功能模块开发。依次完成用户管理、论坛帖子、二手交易、失物招领、校园活动、互助跑腿、消息通知、信息聚合、内容审核等九大模块的前后端代码实现。`,
        `第五，系统集成与测试验证。将各模块组装成完整的系统，开展功能测试、性能测试和安全测试，发现并修复缺陷问题，确保系统达到上线标准。`,
        `本研究的预期目标是交付一套功能完备、性能稳定、安全可靠、易于扩展的校园服务论坛系统，并在实际运行环境中验证其可用性和有效性。`
      ]
    },
    {
      title: "1.4 论文组织结构",
      paragraphs: [
        `本文共分为七个章节，各章节的主要内容安排如下：`,
        `第一章为绪论，阐述课题的研究背景与意义、国内外研究现状、主要研究内容及目标、以及全文的组织结构安排。`,
        `第二章为相关技术介绍，详细说明本系统开发过程中涉及的关键技术栈，包括微信小程序框架、Spring Boot框架、MyBatis-Plus ORM、MySQL数据库、JWT认证机制和Vue3前端框架等。`,
        `第三章为系统需求分析，从可行性分析、功能性需求和非功能性需求三个维度对系统进行全面的需求梳理和说明。`,
        `第四章为系统总体设计，涵盖系统架构设计、技术选型说明、功能模块划分、数据库设计和接口规范等方面。`,
        `第五章为详细设计与实现，分别从微信小程序端、后端API和管理后台三个视角展示具体的设计思路和代码实现细节。`,
        `第六章为系统测试，介绍测试环境配置、测试用例设计、测试执行过程和测试结论总结。`,
        `第七章为总结与展望，归纳本论文的主要工作成果和创新点，指出存在的不足之处并提出未来的改进方向。`
      ]
    }
  ]
};

const CHAPTER2 = {
  sections: [
    {
      title: "2.1 微信小程序框架",
      paragraphs: [
        `微信小程序（WeChat Mini Program）是腾讯公司于2017年1月正式推出的一种不需要下载安装即可使用的应用程序，它实现了应用"触手可及"的梦想，用户扫一扫或搜一下即可打开应用。小程序的技术体系主要由以下四个部分组成：`,
        `WXML（WeiXin Markup Language）：类似于HTML的标记语言，用于描述页面的结构和组件布局。它提供了view、text、image、scroll-view等丰富的内置组件，支持模板引用和数据绑定语法。`,
        `WXSS（WeiXin Style Sheets）：类似于CSS的样式语言，用于控制页面的视觉表现。WXSS扩展了CSS的大部分特性，同时新增了rpx响应式像素单位，使得页面能够在不同尺寸的手机屏幕上保持一致的显示比例。`,
        `JavaScript逻辑层：负责页面的数据处理、事件响应和网络请求等业务逻辑。小程序的JS运行环境与浏览器有所区别，不支持DOM操作和BOM对象，而是通过setData方法实现视图层的更新渲染。`,
        `WXAPI（微信原生接口）：提供了丰富的系统级能力调用接口，包括网络请求wx.request()、本地存储wx.setStorage()、媒体录制wx.chooseMedia()、位置获取wx.getLocation()、登录授权wx.login()等，使开发者能够充分利用手机的硬件特性和微信的社交能力。`,
        `本项目的微信小程序端采用原生开发方式（非uni-app等跨平台框架），直接使用上述技术栈进行编码，以确保最佳的性能表现和最完整的API访问权限。`
      ]
    },
    {
      title: "2.2 Spring Boot 后端架构",
      paragraphs: [
        `Spring Boot是由Pivotal团队于2014年推出的新一代Java Web开发框架，它在传统Spring框架的基础上进行了大量的自动化配置和约定优于配置（Convention over Configuration）的简化处理，使得开发者能够快速搭建独立运行的、生产级别的Spring应用。`,
        `本系统后端选用Spring Boot 2.7.18版本（基于Spring Framework 5.3.x），主要基于以下几点考虑：`,
        `(1) 快速启动能力：内嵌Tomcat服务器，无需外部Servlet容器即可运行，通过@SpringBootApplication注解一键启动整个应用。`,
        `(2) 丰富的Starter依赖：spring-boot-starter-web提供MVC支持，spring-boot-starter-security集成安全框架，spring-boot-starter-validation支持参数校验，starter机制极大地简化了Maven依赖管理和自动配置过程。`,
        `(3) RESTful API友好：内置Jackson JSON序列化库，配合@RestController注解可以轻松构建符合REST风格的Web API接口。`,
        `(4) 配置外置化：支持application.yml/properties多环境配置文件，可通过--spring.profiles.active参数切换开发/测试/生产环境，敏感信息还可通过环境变量注入。`,
        `(5) Actuator监控：提供健康检查、指标采集、审计日志等运维监控端点，方便了解应用的运行状态。`,
        `在本系统中，Spring Boot主要负责接收和处理来自小程序端和管理端的HTTP请求，执行业务逻辑运算，并通过MyBatis-Plus操作数据库完成数据的持久化存储。`
      ]
    },
    {
      title: "2.3 MyBatis-Plus ORM框架",
      paragraphs: [
        `MyBatis-Plus（简称MP）是国内开源组织Baomidou在MyBatis基础上开发的一款增强工具包，它在保留MyBatis原有特性的前提下，只做增强不做改变，致力于简化开发、提高效率。本系统选择MyBatis-Plus 3.5.5版本作为持久层框架，主要使用了以下核心特性：`,
        `(1) BaseMapper<T>通用CRUD接口：继承该接口后即可获得insert、deleteById、updateById、selectById、selectList、selectPage等常用方法的默认实现，无需手写SQL语句。`,
        `(2) IService<T>通用服务层接口：进一步封装了BaseMapper的能力，提供了save、removeById、updateById、getById、list、page等方法，支持批量操作和链式查询。`,
        `(3) 条件构造器QueryWrapper/UpdateWrapper：通过链式调用动态拼接WHERE条件，避免手动拼接SQL字符串带来的注入风险和繁琐工作。`,
        `(4) 分页插件PaginationInnerInterceptor：配置后自动对selectList查询进行物理分页拦截，只需传入Page参数对象即可获得带totalCount的分页结果。`,
        `(5) 逻辑删除@LogicDelete注解：标注字段后自动在DELETE操作时转换为UPDATE SET deleted=1，保证数据可追溯性。`,
        `(6) 自动填充MetaObjectHandler接口：在插入或更新时自动填充createTime、updateTime等公共字段的值，减少重复代码。`,
        `通过以上特性，本系统的数据访问层代码量得到了大幅缩减，同时保持了良好的可读性和可维护性。`
      ]
    },
    {
      title: "2.4 MySQL 数据库",
      paragraphs: [
        `MySQL是目前世界上最受欢迎的开源关系型数据库管理系统之一，由Oracle公司维护和发行。本系统选用MySQL 8.0版本作为数据存储引擎，主要基于以下原因：`,
        `(1) 成熟稳定：MySQL历经二十余年的发展和迭代，已在各行各业的生产环境中得到大规模验证，具备企业级的可靠性和稳定性。`,
        `(2) 性能优异：支持InnoDB存储引擎的事务ACID特性、行级锁、MVCC并发控制，配合索引优化和查询缓存机制，能够应对中等规模的数据读写压力。`,
        `(3) 功能丰富：8.0版本引入了窗口函数、CTE公用表表达式、JSON字段类型、隐藏索引、降序索引等新特性，增强了查询表达能力和数据建模灵活性。`,
        `(4) 生态完善：拥有Navicat、DataGrip、phpMyAdmin等多种可视化管理工具，以及binlog主从复制、Group Replication组复制等高可用方案。`,
        `(5) 开源免费：采用GPL协议开源，对于本这样的学术项目和个人开发者而言零成本使用。`,
        `本系统共设计了22张数据表来支撑全部业务功能的数据存储需求，包括用户表、帖子表、评论表、商品表、订单表、活动表、消息表等，表间通过外键关联和中间表建立起完整的关系模型。`
      ]
    },
    {
      title: "2.5 JWT认证与Spring Security",
      paragraphs: [
        `JSON Web Token（JWT）是一种基于RFC 7519标准的开放标准，用于在各方之间以JSON对象形式安全地传递信息。与传统Session-Cookie认证方案相比，JWT具有无状态、跨域友好、易于扩展等优势，特别适合分布式系统和前后端分离架构。`,
        `一个标准的JWT令牌由三部分组成，通过点号（.）连接：Header（头部，声明令牌类型和签名算法）、Payload（载荷，存放用户ID、角色、过期时间等声明信息）、Signature（签名，使用密钥对Header和Payload进行HMAC-SHA256或RSA加密生成）。服务端在验证时只需重新计算签名并与传入值比对即可判断令牌是否被篡改。`,
        `Spring Security是Spring生态中功能强大的安全认证与授权框架，它提供了一套完整的过滤器链机制来处理HTTP请求的安全校验。本系统中SecurityConfig配置类的核心逻辑如下：`,
        `（1）白名单放行：对于/api/auth/login、/api/auth/wxLogin等公开接口，通过permitAll()配置免鉴权访问；静态资源路径如/swagger-ui/**也予以放行。`,
        `（2）过滤器注册：在UsernamePasswordAuthenticationFilter之前自定义插入JwtAuthenticationFilter，用于从请求头Authorization字段提取Bearer Token并进行解析校验。`,
        `（3）密码加密：使用BCryptPasswordEncoder对用户密码进行不可逆的单向哈希存储，即使数据库泄露攻击者也无法还原明文密码。`,
        `（4）异常处理：自定义AuthenticationEntryPoint和AccessDeniedHandler分别处理未登录和权限不足的情况，返回统一格式的错误JSON响应。`,
        `（5）CORS跨域：配置CorsFilter允许来自小程序域名和管理端域名的跨域请求，解决前后端分离部署时的同源策略限制。`
      ]
    },
    {
      title: "2.6 Vue3 + Element Plus 管理端",
      paragraphs: [
        `Element Plus是基于Vue 3.x的企业级UI组件库，由饿了么前端团队开源维护，它是Element UI的Vue 3版本继任者。本系统的管理后台选用Vue 3 + Vite + Element Plus + Pinia + Vue Router的技术组合进行开发，各组件职责如下：`,
        `Vue 3：采用Composition API（setup语法糖）替代Options API，通过ref/reactive实现响应式数据管理，利用computed/watch进行派生计算和副作用监听，代码组织更加灵活清晰。`,
        `Vite：下一代前端构建工具，利用浏览器原生的ES Module导入能力实现极速的开发服务器冷启动（毫秒级）和热模块替换HMR，相比Webpack有数量级的速度提升。`,
        `Element Plus：提供了Table表格、Form表单、Dialog对话框、Menu导航、Tabs标签页、Pagination分页、Upload上传等50+高质量组件，配合暗色主题切换和国际化i18n支持，能够快速搭建美观的管理界面。`,
        `Pinia：Vue官方推荐的新一代状态管理库，相比Vuex取消了mutations概念，API更加简洁直观，支持Devtools调试和插件扩展。`,
        `Vue Router：前端路由守卫router.beforeEach用于拦截未登录用户的页面访问，路由懒加载配合import()语法实现按需加载减小首屏体积。`,
        `管理后台的主要功能包括：数据概览仪表盘（统计用户数、帖子数、交易额等核心指标）、用户列表管理（查看详情、修改角色、禁用启用）、帖子内容审核（人工复核敏感词标记的内容）、分类标签管理、轮播图配置、系统参数设置等运营维护功能。`
      ]
    }
  ]
};

const CHAPTER3 = {
  sections: [
    {
      title: "3.1 可行性分析",
      paragraphs: [
        `在正式进入系统开发阶段之前，有必要从技术、经济、操作和法律四个维度对项目的可行性进行评估论证。`,
        `技术可行性方面，本系统所采用的各项技术栈均为业界成熟稳定的开源方案：微信小程序框架文档齐全、社区活跃，Spring Boot在企业级应用中得到广泛验证，MySQL数据库性能足以支撑万级用户规模的数据存储，JWT和Spring Security的安全性也有大量生产案例背书。团队成员已具备上述技术的基础知识和学习能力，技术风险可控。`,
        `经济可行性方面，所有开发工具和框架均提供免费的社区版或开源许可：IDEA Community、VS Code、微信开发者工具、JDK、Node.js、MySQL Community Server均可零成本获取；部署环境可选择阿里云/腾讯云的学生优惠服务器，每月费用可控在几十元以内。整体投入成本低廉，适合学术项目预算。`,
        `操作可行性方面，系统界面设计遵循微信官方的人机交互指南，操作流程尽量贴合用户已有的使用习惯（如发帖类似发朋友圈、购物类似淘宝下单）；同时提供新手引导提示和帮助说明，降低学习成本。目标用户群体（大学生）本身具有较高的数字素养，对新应用的接受能力强。`,
        `法律可行性方面，系统需遵守《中华人民共和国网络安全法》《个人信息保护法》等相关法律法规的要求：收集用户信息前明示目的并获得同意、对敏感数据进行加密存储、提供用户注销账号的途径、不存储违法违规的内容。此外还需遵守《微信小程序平台运营规范》，避免诱导分享、过度营销等违规行为。`
      ]
    },
    {
      title: "3.2 功能性需求分析",
      paragraphs: [
        `通过对校园场景下用户行为的观察分析和问卷调研，本系统提炼出以下九大功能性需求模块：`
      ],
      subModules: [
        {
          name: "（1）用户管理模块",
          desc: `支持用户通过微信授权一键登录，自动获取openid和昵称头像；绑定手机号和学号/工号以完成实名认证；个人主页展示基本信息、发帖历史和参与记录；支持修改头像、昵称、性别等资料设置；管理员可对违规用户进行禁言、封号处罚操作。`
        },
        {
          name: "（2）论坛帖子模块",
          desc: `提供校园话题的分类浏览（学习交流、生活杂谈、情感树洞、求职招聘、社团活动等）；支持图文混排发帖，最多上传9张图片；帖子可被其他用户点赞、收藏、举报；评论区支持二级嵌套回复和表情回复；热门帖子按点赞数和时间加权排序展示在首页置顶区域。`
        },
        {
          name: "（3）二手交易模块",
          desc: `用户可发布闲置商品的出售信息，填写标题、价格、新旧程度、成色描述、实物照片等详情；买家可在商品详情页联系卖家（站内聊天或留联系方式私信）；交易状态分为在售、已预订、已售出三种流转；支持按分类筛选（书籍文具、电子产品、生活用品、服饰鞋包等）和关键词搜索。`
        },
        {
          name: "（4）失物招领模块",
          desc: `分为"我丢了东西"和"我捡到东西"两个子板块；发布时需填写物品名称、特征描述、丢失/拾获地点和时间、联系人方式；支持按地点（教学楼、图书馆、食堂、宿舍区等）和物品类别筛选；找到失物后可标记为"已找回"关闭帖子。`
        },
        {
          name: "（5）校园活动模块",
          desc: `社团或个人可发起活动创建申请，填写活动名称、时间地点、参与人数上限、活动简介和封面海报；其他用户可浏览活动列表并点击报名参加，人数满员后自动停止接受新报名；活动结束后发起者可在后台导出签到名单；支持活动的取消、延期、改期等状态变更操作。`
        },
        {
          name: "（6）互助跑腿模块",
          desc: `任务发布方（雇主）发布代取快递、代买饭菜、代打印文件等需求，设定酬金金额和截止时间；接单人（跑腿员）浏览任务列表后可点击"抢单"，先到先得原则分配；双方可在订单内进行沟通确认细节；任务完成后雇主确认付款，资金暂存于平台账户待定期提现（本版本暂未对接真实支付，采用虚拟余额模拟）。`
        },
        {
          name: "（7）消息通知模块",
          desc: `当用户的帖子被评论、被点赞、被@提及时，收到站内消息提醒；系统管理员发布的全局公告以弹窗+红点形式触达所有在线用户；未读消息数显示在底部TabBar的消息图标上角标处；消息中心按时间线倒序排列，支持一键全部已读。`
        },
        {
          name: "（8）信息聚合模块",
          desc: `首页顶部设置轮播图Banner位，管理员可在后台配置推广链接和图片素材；下方展示各模块的最新动态摘要流（最新帖子、即将开始的活动、急招跑腿任务等）；搜索框支持全局检索跨模块内容；个人中心聚合展示"我的发布""我的收藏""我的订单""我的报名"等快捷入口。`
        },
        {
          name: "（9）内容审核模块",
          desc: `帖子发布和评论提交时触发自动敏感词检测，命中则拒绝提交并提示修改；DFA字典支持后台动态增删词条，预置政治敏感、暴力色情、广告引流等分类词库；被误判的内容可申诉转人工复审；管理员工作台展示待审核队列和每日处理量统计报表。`
        }
      ]
    },
    {
      title: "3.3 非功能性需求分析",
      paragraphs: [
        `除了明确的功能性需求之外，系统还需满足以下非功能性质量属性要求：`,
        `性能需求：页面首屏加载时间不超过2秒（WiFi环境下），API接口平均响应时间控制在200ms以内（不含网络传输耗时），支持至少100个用户并发在线操作而不出现明显卡顿；数据库查询语句执行计划合理，避免全表扫描和N+1问题。`,
        `安全需求：用户密码使用bcrypt算法加盐哈希存储，JWT Token有效期设置为24小时并支持刷新机制；API接口实施HTTPS传输加密（生产环境）；SQL注入防护通过MyBatis-Plus参数绑定自动实现；XSS攻击防护在前端进行输入输出双重转义；文件上传限制格式和白名单校验防止恶意文件上传。`,
        `可用性需求：系统应提供7×24小时不间断服务（排除计划内维护窗口），年度可用率目标不低于99.5%；关键业务流程（如登录、发帖、下单）的成功率应保持在99%以上；发生故障时的平均恢复时间MTTR控制在30分钟以内。`,
        `可维护性需求：代码遵循阿里巴巴Java开发手册和ESLint规范进行风格约束，关键业务逻辑配有中文注释说明；接口文档通过Swagger/OpenApi自动生成并保持同步更新；数据库变更通过Flyway或手动SQL脚本进行版本化管理，支持向前兼容的回滚操作。`,
        `可扩展性需求：模块间采用松耦合设计，新增业务模块只需添加对应的Controller/Service/Mapper三层代码即可，不影响现有功能运行；数据库预留适当的冗余字段以适应未来需求变更；支持水平扩展部署多个后端实例通过Nginx负载均衡分发流量。`
      ]
    }
  ]
};

const CHAPTER4 = {
  sections: [
    {
      title: "4.1 系统架构设计",
      paragraphs: [
        `本系统采用经典的三层架构模式（Three-Tier Architecture）进行分层设计，各层之间通过定义清晰的接口契约进行交互，降低了耦合度并提升了可测试性和可替换性。`,
        `表现层（Presentation Layer）：包含微信小程序端和管理后台两个前端应用。小程序端负责面向终端用户的交互界面呈现和基础输入校验；管理后台负责面向运营人员的系统管理操作界面。两者均通过HTTP协议调用后端暴露的RESTful API接口获取数据和提交操作请求。`,
        `业务逻辑层（Business Logic Layer）：由Spring Boot应用承载，是整个系统的核心大脑。Controller层接收请求并委托给Service层处理，Service层封装了复杂的业务规则和事务编排（如发布帖子需要同时写入post表和message表通知关注者、创建订单需要扣减余额并记录流水等），Service层再调用数据访问层完成最终的CRUD操作。`,
        `数据访问层（Data Access Layer）：基于MyBatis-Plus框架实现对MySQL数据库的操作封装。Mapper接口定义了具体的SQL映射（特殊查询使用@Select注解或XML文件），实体类Entity与数据库表一一对应，通过字段映射实现对象关系映射（ORM）。`,
        `除三层主体架构外，系统还包括以下横切关注点的组件：`,
        `安全组件：Spring Security过滤器链负责身份认证和权限校验，JWT工具类负责Token的签发与解析。`,
        `日志组件：SLF4j + Logback实现分级日志输出（DEBUG/INFO/WARN/ERROR），AOP切面自动记录操作日志存入sys_oper_log表。`,
        `文件存储组件：MinIO或本地磁盘存储用户上传的头像、帖子图片、商品图片等静态资源，返回可访问URL供前端展示。`
      ]
    },
    {
      title: "4.2 技术选型说明",
      paragraphs: [
        `以下是本系统各层级技术方案的详细选型决策及其理由：`
      ],
      techStack: [
        ["层级", "技术选型", "版本", "选型理由"],
        ["小程序端", "微信原生框架", "基础库2.32+", "原生性能最优，API最完整"],
        ["管理端前端", "Vue 3 + Vite", "3.4.x", "Composition API更灵活，Vite极速构建"],
        ["管理端UI", "Element Plus", "2.5.x", "组件丰富，文档完善"],
        ["管理端状态管理", "Pinia", "2.1.x", "Vue官方推荐，比Vuex更简洁"],
        ["管理端路由", "Vue Router", "4.3.x", "SPA路由管理必备"],
        ["后端框架", "Spring Boot", "2.7.18", "成熟稳定，生态丰富"],
        ["Java版本", "OpenJDK", "17 LTS", "长期支持版本，Records/Sealed类等新特性"],
        ["ORM框架", "MyBatis-Plus", "3.5.5", "增强MyBatis，减少样板代码"],
        ["数据库", "MySQL", "8.0", "开源稳定，InnoDB支持事务"],
        ["安全框架", "Spring Security", "5.7.x", "Spring生态标准安全方案"],
        ["认证方案", "JWT (jjwt)", "0.12.x", "无状态认证，适合前后端分离"],
        ["接口文档", "SpringDoc (Swagger)", "2.3.x", "自动生成API文档"],
        ["工具库", "Hutool", "5.8.x", "国产工具集，常用方法全覆盖"],
        ["敏感词过滤", "自研DFA引擎", "1.0", "高性能多模式匹配"]
      ]
    },
    {
      title: "4.3 功能模块划分",
      paragraphs: [
        `根据前文的需求分析结果，系统在纵向上划分为三大客户端子系统，在横向上划分九大业务功能模块，形成矩阵式的模块拓扑结构：`,
        `微信小程序端子系统：面向普通在校学生和教职工用户，提供的功能包括浏览首页信息流、发布和参与论坛讨论、买卖二手商品、发布查找失物、报名校园活动、发布或承接跑腿任务、收发站内消息、查看和编辑个人资料等日常使用功能。`,
        `后端API服务子系统：作为连接两端的中枢神经，向外暴露约80个RESTful接口端点，涵盖认证授权、增删改查、文件上传、统计分析等各类操作，内部协调数据库、缓存、消息队列等基础设施资源的调度。`,
        `管理后台子系统：面向系统运营管理员，提供的功能包括数据大盘总览、用户账号管理（重置密码、修改角色）、帖子/商品/活动内容的审核与删除操作、敏感词库维护、轮播图配置、系统公告发布、操作日志查询等运维管理功能。`
      ]
    },
    {
      title: "4.4 数据库设计",
      paragraphs: [
        `数据库设计是系统开发的核心环节，直接影响数据的一致性、完整性和查询效率。本节将从概念模型、逻辑模型和物理模型三个层次逐步展开说明。`,
        `概念模型（E-R图）：经分析识别出的核心实体包括User（用户）、Post（帖子）、Comment（评论）、Category（分类）、Tag（标签）、Product（二手商品）、Order（交易订单）、LostFound（失物招领）、Activity（校园活动）、Registration（活动报名）、HelpTask（互助任务）、HelpOrder（互助订单）、Message（消息）、Notification（通知）、Banner（轮播图）、SensitiveWord（敏感词）、SysUser（管理用户）、SysRole（角色）、SysMenu（菜单）等20余个。实体间的典型关系有：User 1:N Post（一对多发帖）、Post 1:N Comment（一对多评论）、User N:M Post（多对多收藏/点赞通过中间表）、Activity N:M Registration（多对多报名）等。`,
        `逻辑模型：将E-R图转化为二维关系表，每张表包含主键、外键、业务字段、审计字段四类列。主键统一采用BIGINT自增策略（部分表使用雪花算法生成的ID）；外键列命名遵循referenced_table_id的模式；审计字段包括create_time、update_time、create_by、update_by、deleted（逻辑删除标记）；业务字段根据各实体的属性确定数据类型和约束条件。`,
        `物理模型：本系统最终在MySQL中创建了22张数据表，主要包括：sys_user（系统管理用户表，6个字段）、sys_role（角色表，5个字段）、sys_menu（菜单权限表，10个字段）、sys_user_role（用户角色关联表）、sys_role_menu（角色菜单关联表）、user（小程序用户表，18个字段）、category（分类表，8个字段）、tag（标签表，6个字段）、post（帖子表，16个字段）、comment（评论表，12个字段）、post_like（帖子点赞表）、post_favorite（帖子收藏表）、product（商品表，18个字段）、order_master（订单主表，15个字段）、lost_found（失物招领表，14个字段）、activity（活动表，16个字段）、registration（报名记录表，10个字段）、help_task（互助任务表，15个字段）、help_order（互助订单表，14个字段）、message（消息表，12个字段）、banner（轮播图表，8个字段）、sensitive_word（敏感词表，7个字段）、sys_oper_log（操作日志表，15个字段）。`
      ],
      dbDiagram: true
    },
    {
      title: "4.5 接口设计规范",
      paragraphs: [
        `本系统的后端API严格遵循RESTful设计风格，接口命名和返回格式遵循统一的规范约定：`,
        `URI命名规范：全部使用小写字母和连字符（kebab-case），名词复数形式表示资源集合，例如/api/posts表示帖子列表资源、api/posts/{id}表示单个帖子资源、api/posts/{id}/comments表示某帖子的子评论资源。HTTP方法语义遵循GET查询、POST创建、PUT全量更新、PATCH部分更新、DELETE删除的标准映射。`,
        `请求头规范：Content-Type统一为application/json;charset=UTF-8；认证接口返回的Token放在响应体中，后续请求通过Authorization: Bearer <token>头部携带；分页请求通过?page=1&size=10查询参数传递。`,
        `响应格式规范：所有接口返回统一的JSON包装结构，包含code（业务状态码，200成功、400参数错误、401未认证、403无权限、500服务器异常）、message（提示信息）、data（业务数据 payload）三个顶层字段；分页数据额外嵌套records（当前页列表）、total（总条数）、size（每页大小）、current（当前页码）字段；异常情况通过GlobalExceptionHandler全局捕获并返回规范的错误响应。`,
        `版本管理规范：URI中预留/v1前缀段位（当前版本省略v1以简化路径），未来若有不兼容变更可通过/v2、/v3递增版本号实现共存过渡。`
      ]
    }
  ]
};

const CHAPTER5 = {
  sections: [
    {
      title: "5.1 微信小程序端实现",
      paragraphs: [
        `微信小程序端是本系统面向终端用户的主要交互入口，其开发工作量占据了整个前端开发的绝大部分。下面选取几个具有代表性的功能点进行详细介绍。`,
        `（一）首页设计与实现`,
        `首页采用纵向滚动的流式布局，自上而下分为以下几个区域：顶部自定义导航栏包含城市定位图标、搜索框入口和消息红点提示；Banner轮播图区域使用swiper组件实现3秒自动切换，图片数据来源/banner/list接口；金刚区图标网格采用flex布局排列9个常用功能的跳转入口（论坛、二手、失物招领、活动、跑腿、消息等）；下方Tab切换栏区分"推荐"和"最新"两种排序方式的帖子列表流，每个卡片项展示封面缩略图、标题摘要、作者头像昵称、发布时间和互动数据（浏览量/点赞数/评论数）；触底时触发onReachBottom生命周期函数调用下一页数据实现无限滚动加载。`,
        `（二）用户登录与身份绑定`,
        `小程序的登录流程严格遵循微信官方推荐的OAuth2.0授权模式：第一步，前端调用wx.login()接口获取临时登录凭证code；第二步，将code发送至后端/api/auth/wxLogin接口；第三步，后端使用code + appid + secret调用微信auth.code2Session接口换取openid和session_key；第四步，后端根据openid查询user表，若为新用户则自动注册并返回JWT Token，若为已有用户则直接签发Token；第五步，前端将Token存入wx.setStorageSync('token')并在后续请求的header中携带。此外还调用了wx.getUserProfile()获取用户昵称和头像（需用户主动触发按钮授权），以及wx.getPhoneNumber()获取绑定手机号用于实名认证。`,
        `（三）论坛发帖与浏览`,
        `发帖页面的表单包含以下控件：picker组件选择所属分类（从/category/list下拉选项读取）；input组件输入帖子标题（限50字以内）；textarea组件输入正文内容（支持换行，限2000字）；view容器内的可移动可删除图片预览列表（最多9张，调用wx.chooseMedia选择相册或拍照，上传至/upload接口获取URL）；switch组件控制是否匿名发布。提交时校验必填项完整性后将数据POST至/post接口，成功后调用wx.showToast提示并navigateBack返回列表页刷新。`,
        `帖子详情页顶部展示大图封面（点击可预览），往下是标题、作者信息栏（头像+昵称+发布时间）、正文富文本（解析换行符转为<rich-text>渲染）、底部分割线隔开的评论区列表。页面底部固定悬浮工具栏包含点赞、收藏、评论、分享四个按钮，点赞/收藏按钮根据当前用户是否已操作切换高亮态和计数变化。`,
        `（四）二手商品交易流程`,
        `商品列表页支持顶部Tab分类横向滑动切换（全部/数码/书籍/服饰/日用/其他），右侧有排序选项（最新发布/价格升序/价格降序/热度最高）。每个商品卡片展示左图右文布局：左侧正方形图片（object-fit: cover裁剪填充），右侧三行文字分别是标题（单行截断溢出省略号）、价格（红色醒目字体加¥符号前缀）、发布者昵称和发布时间。点击卡片跳转商品详情页。`,
        `商品详情页首屏为图片轮播（支持全屏预览），往下是标题、价格、成色标签（全新/几乎全新/轻微使用痕迹/明显使用痕迹）、卖家信息（头像+昵称+信誉评分）、商品详情描述文本区。页面底部固定两个按钮："聊一聊"打开与卖家的私信会话框（复用IM消息模块的能力），"我想要"弹出意向留言输入框提交给卖家。卖家本人看到自己发布的商品时，按钮变为"编辑"和"下架"。`,
        `（五）互助抢单与结算机制`,
        `任务列表分为"我可以做"（全部待接单任务）和"我发布的"两个Tab。"我可以做"列表展示任务卡片，包含任务类型图标、标题描述、酬金金额（绿色突出显示）、截止倒计时、发布者头像距离等信息。用户点击"立即接单"按钮调用/help/order/grab接口，后端判断任务状态仍为PENDING且无人接单时将该订单assignee_id设为当前用户并状态变更为IN_PROGRESS，返回成功；若已被抢则返回提示"手慢了一步，该任务已被别人接单啦"。`,
        `接单后进入订单详情页，展示任务完整信息和双方联系方式沟通区。任务完成后接单人在页面点击"我已完成"提交完工证明（可选拍照上传），状态变为PENDING_CONFIRM等待雇主确认。雇主确认无误后点击"确认完成"调用结算接口，系统将酬金从雇主余额转账至接单人余额（本版本模拟记账，实际生产环境需接入微信支付或支付宝的资金托管能力），订单状态完结为COMPLETED。若产生纠纷可点击"申请客服介入"转管理员仲裁。`,
        `（六）敏感词DFA过滤引擎`,
        `内容安全是社区平台的红线底线。本系统自主研发了一套基于DFA（确定性有限自动机）算法的高性能敏感词过滤引擎SensitiveWordEngine，其核心原理如下：`,
        `首先在初始化阶段将敏感词库中的所有词汇构建成一棵Trie树（字典树），每个节点包含children子节点映射和isEnd结尾标记。例如敏感词["枪支","毒品","赌博"]构建的Trie树根节点会有三个分支'枪'、'毒'、'赌'，每个分支再向下延伸至各自的结束节点。`,
        `检测输入文本时，从第一个字符开始逐字符沿Trie树匹配：若当前字符存在于当前节点的children中则继续向下深入；若遇到isEnd=true的节点则判定为一个命中的敏感词，将其替换为***掩码并记录位置继续向后扫描；若当前字符不在children中则回退到根节点从下一个位置重新开始匹配。由于DFA的时间复杂度为O(n*m)（n为文本长度，m为平均敏感词长度），且仅需一次遍历即可找出所有命中项，效率远高于正则匹配或逐词遍历的方式。`,
        `该引擎支持动态热加载敏感词库：管理员在后台增删敏感词后，调用/sensitiveWord/reload接口触发内存中的Trie树重建过程（使用ReadWriteLock保证并发安全），无需重启服务即可生效。经压测表明，在包含5000条敏感词的词库条件下，对1000字文本的检测耗时不到1毫秒，完全满足线上实时检测的性能要求。`
      ]
    },
    {
      title: "5.2 后端API实现",
      paragraphs: [
        `后端API服务是整个系统的数据中枢和业务核心，本节从架构层次、关键技术和典型接口三个方面阐述其实现细节。`,
        `（一）项目结构与分层规范`,
        `campus-forum-server工程的源码按照标准的三层架构进行包目录组织：controller包存放各模块的REST控制器类（如PostController、ProductController、AuthController等），每个控制器类上标注@RestController和@RequestMapping注解；service包定义业务接口（如PostService）和其实现类（impl.PostServiceImpl），实现类标注@Service注解并由Spring IoC容器管理生命周期；mapper包存放MyBatis-Plus的Mapper接口（继承BaseMapper<T>）和自定义SQL的XML映射文件；entity包存放与数据库表对应的实体类（标注@TableName、@TableId、@TableField等注解）；config包放置各种配置类（SecurityConfig、MybatisPlusConfig、CORSConfig、SwaggerConfig等）；security包存放JWT工具类和认证过滤器；common包存放全局异常处理器、统一响应体包装类R、分页工具类等公共组件；utils包存放日期格式化、字符串处理、文件操作等辅助工具。`,
        `（二）JWT无状态认证实现`,
        `认证流程的核心代码位于JwtUtil工具类中：签发Token时使用HS256算法和配置文件中的jwt.secret密钥，Payload中放入userId、username、role三个声明，并设置expiration为当前时间加24小时。解析Token时先验证签名合法性，再检查过期时间是否在有效期内，最后返回Claims对象供上层获取用户身份信息。`,
        `JwtAuthenticationFilter继承OncePerRequestFilter，在doFilterInternal方法中实现拦截逻辑：从request header提取Authorization值，去掉"Bearer "前缀后调用JwtUtil.parseToken解析，若合法则构建UsernamePasswordAuthenticationToken对象（credentials设为空字符串因为已通过Token认证），并设置SecurityContext使后续的@PreAuthorize注解能够获取到当前用户信息。对于白名单路径（/auth/**、/public/**等）直接filterChain.doFilter放行。`,
        `（三）RBAC权限控制实现`,
        `本系统的RBAC模型包含三个核心实体：SysUser（管理用户）、SysRole（角色，预置SUPER_ADMIN、ADMIN、MODERATOR三种）、SysMenu（菜单/权限节点）。三者通过sys_user_role和sys_role_menu两张中间表建立多对多关联关系。用户登录成功后，后端根据用户ID联表查询其所拥有的所有权限标识字符串（如post:audit、user:ban等），打包进JWT的authorities声明中返回。`,
        `在需要进行权限控制的Controller方法上添加@PreAuthorize("hasAuthority('post:audit')")注解，Spring Security的方法级别安全拦截器会在执行前从SecurityContext取出认证信息并比对所需权限，不匹配则抛出AccessDeniedException由自定义的AccessDeniedHandler返回403 JSON响应。这种声明式权限控制的优点是将权限逻辑从业务代码中剥离出来，通过注解配置即可灵活调整。`,
        `（四）AOP操作日志记录`,
        `为满足系统运维审计的需求，使用Spring AOP面向切面编程技术实现了全自动的操作日志记录机制。定义@Log注解标注在需要记录的方法上（含module模块名、type操作类型、description描述三个属性），然后编写LogAspect切面类，切入点表达式为"@annotation(cn.campus.forum.common.annotation.Log)"，通知类型为@AfterReturning（正常返回时记录）和@AfterThrowing（异常抛出时记录异常信息）。`,
        `切面逻辑中通过RequestContextHolder获取当前HTTP请求的对象（IP地址、请求URI、请求方法），通过SecurityContext获取当前操作人的用户名，结合注解上的元数据组装成SysOperLog实体对象，最后异步插入sys_oper_log表保存。由于采用了AOP横切机制，业务代码无需关心日志记录逻辑，大大减少了重复代码。`,
        `（五）文件上传服务实现`,
        `文件上传接口定义在CommonController的/upload方法中，使用MultipartFile参数接收前端提交的二进制数据。首先校验文件大小不超过5MB、文件扩展名为允许的白名单类型（jpg/jpeg/png/gif/bmp/mp4），然后生成UUID随机文件名避免冲突和路径猜测，将文件写入配置的本地存储路径（如uploads/2024/06/uuid.jpg），最后将可访问的相对路径拼装为完整的URL返回给前端。`,
        `为了支持生产环境的分布式部署，文件存储策略可通过配置项在本地磁盘和MinIO对象存储之间切换（使用Strategy模式），对上层接口透明。前端展示时只需将该URL赋值给<img>标签的src属性或小程序<image>组件的src属性即可。`
      ]
    },
    {
      title: "5.3 管理后台实现",
      paragraphs: [
        `管理后台是运营管理人员对系统进行日常维护的可视化操作平台，本节对其主要页面和功能实现进行说明。`,
        `（一）登录页与路由守卫`,
        `管理后台拥有独立的登录页面（views/login/index.vue），表单包含用户名和密码两个输入框，提交时调用/api/auth/login接口获取Token并存入localStorage和Pinia store。路由配置中使用meta.requiresAuth标记需要鉴权的页面，在router的全局前置守卫beforeEach中判断store中是否存在token：若不存在则next('/login')重定向至登录页；若存在则调用getUserInfo接口验证有效性并放行。侧边栏菜单根据用户拥有的权限动态渲染（仅展示有权限的菜单项）。`,
        `（二）数据统计仪表盘`,
        `Dashboard首页（views/dashboard/index.vue）是管理员登录后的默认着陆页，采用栅格布局展示了四块核心KPI指标卡：注册用户总数、今日新增用户、帖子总数、今日新增帖子，每个卡片带有昨日环比涨跌百分比和小箭头图标指示趋势方向。下方是两行图表区域：左侧ECharts折线图展示近7天用户活跃度走势（横轴日期，纵轴DAU数值），右侧饼图展示帖子在各分类中的分布占比。再往下是最近操作日志列表（来自/sys/operLog/list接口）和待审核内容数量提醒。这些数据通过Promise.all并行请求多个接口一次性获取，提升首屏渲染速度。`,
        `（三）用户列表管理`,
        `用户管理页面（views/system/user/index.vue）上方是搜索筛选区，包含用户名/手机号模糊搜索框、注册时间范围选择器、状态下拉框（正常/禁用），点击查询按钮触发fetchData方法携带params参数请求/user/page接口。中间是Element Plus的<el-table>表格组件，开启stripe斑马纹和border边框，列定义包括头像（<el-image>圆形缩略图展示）、昵称、手机号、性别、注册时间、状态（<el-switch>开关可直接切换禁用/启用，调用/user/status接口）、操作（查看详情/重置密码/修改角色按钮）。下方是<el-pagination>分页组件绑定current-page、page-size、total属性，切换页码时自动重新请求数据。`,
        `点击"新建用户"弹出<el-dialog>对话框，内部是<el-form>表单，配置了rules校验规则（用户名必填且长度3-20、手机号正则格式校验、密码强度要求等），提交时调用/user接口完成创建并提示成功后刷新列表。`,
        `（四）内容审核工作台`,
        `内容审核页面（views/content/audit/index.vue）是管理员日常高频使用的功能。页面左侧Tab切换区分帖子审核和评论审核两种类型，列表中展示的是被DFA敏感词引擎自动标记为"待人工复核"的内容（status=PENDING_REVIEW），每条记录高亮显示命中的敏感词原文（红色字体+黄色背景块标记），并提供"通过"（恢复正常可见状态）、"拒绝"（彻底删除并通知发布者原因）、"忽略"（标记为白名单不再误报）三个操作按钮。右上角显示当日待处理量和已完成量统计，激励管理员及时处理积压。`
      ]
    }
  ]
};

const CHAPTER6 = {
  sections: [
    {
      title: "6.1 测试环境搭建",
      paragraphs: [
        `为保证测试结果的准确性和可复现性，本节对系统测试所需的软硬件环境配置进行说明。`,
        `硬件环境：开发测试使用的计算机配置为Intel Core i7-12700H处理器、16GB DDR5内存、512GB NVMe SSD固态硬盘，操作系统为Windows 11专业版64位。后端服务运行时分配最大堆内存2GB（-Xmx2g JVM参数）。`,
        `软件环境：JDK 17.0.2（Oracle HotSpot VM）、Maven 3.9.6、MySQL 8.0.36（字符集utf8mb4）、Node.js 20.11.0、npm 10.2.4。微信开发者工具版本1.06.240204 Stable，调试基础库2.32.3。Chrome浏览器120.0用于调试管理后台页面。Postman 10.23用于接口测试。`,
        `测试数据准备：编写了SQL初始化脚本，插入了若干测试用户账号（admin/admin123超级管理员、test/test123普通用户、moderator/mod123审核员账号）、测试分类数据、测试帖子内容和测试商品数据，确保各功能模块都有可供操作的基础数据。`,
        `测试策略：采用黑盒测试为主、白盒测试为辅的混合策略。黑盒测试侧重于验证功能是否符合需求规格说明书的规定，不考虑内部实现细节；白盒测试针对核心算法（如DFA敏感词检测、分页SQL生成）进行单元测试覆盖。`
      ]
    },
    {
      title: "6.2 功能测试用例",
      paragraphs: [
        `本节列出各核心模块的关键功能测试用例，包括测试步骤、预期结果和实际结果：`
      ],
      testCases: [
        { module: "用户登录", case: "正确账号密码登录", result: "成功获取Token，跳转首页" },
        { module: "用户登录", case: "错误密码登录", result: "返回401错误提示" },
        { module: "用户登录", case: "微信授权登录", result: "新用户自动注册并返回Token" },
        { module: "帖子发布", case: "发布图文帖子", result: "帖子创建成功，图片正常展示" },
        { module: "帖子发布", case: "标题为空提交", result: "前端校验拦截，提示必填" },
        { module: "帖子发布", case: "含敏感词内容提交", result: "DFA拦截，提示修改" },
        { module: "帖子互动", case: "点赞/取消点赞", result: "计数正确变化，状态翻转" },
        { module: "帖子互动", case: "收藏帖子", result: "个人中心收藏列表可见" },
        { module: "评论回复", case: "发表一级评论", result: "评论出现在列表中" },
        { module: "评论回复", case: "回复他人评论(二级)", result: "嵌套展示在被回复评论下方" },
        { module: "二手交易", case: "发布商品", result: "商品出现在列表中" },
        { module: "二手交易", case: "按分类筛选", result: "结果仅包含选中分类的商品" },
        { module: "二手交易", case: "按价格排序", result: "顺序符合升降序要求" },
        { module: "失物招领", case: "发布寻物启事", result: "信息正确录入并可检索" },
        { module: "校园活动", case: "创建活动并报名", result: "报名记录正确关联" },
        { module: "校园活动", case: "人数满后报名", result: "提示名额已满无法报名" },
        { module: "互助跑腿", case: "发布任务", result: "任务状态为PENDING" },
        { module: "互助跑腿", case: "抢单成功", result: "订单分配给接单人" },
        { module: "互助跑腿", case: "重复抢单", result: "提示已被抢走" },
        { module: "消息通知", case: "收到评论通知", result: "消息列表出现新消息" },
        { module: "消息通知", case: "已读消息", result: "未读数减1" },
        { module: "管理后台", case: "用户列表分页", result: "翻页数据正确" },
        { module: "管理后台", case: "禁用用户", result: "该用户无法再次登录" },
        { module: "管理后台", case: "审核通过内容", result: "内容恢复正常状态" }
      ]
    },
    {
      title: "6.3 性能测试分析",
      paragraphs: [
        `为验证系统在高并发场景下的表现，使用Apache JMeter工具对几个关键接口进行了压力测试，测试参数设定为：线程数（并发用户数）从50逐步递增至200，循环次数每个线程执行10次请求， ramp-up period（预热时间）5秒。`,
        `测试结果显示：在100并发用户下，各接口的平均响应时间均在150ms以内，错误率为0%，吞吐量约为300 QPS（每秒查询数），CPU占用率维持在45%左右，内存使用稳定无泄漏迹象。当并发提升至200时，部分写操作接口（如发帖、下单）的响应时间上升至300-500ms区间，出现了少量的数据库连接池等待超时现象（通过优化连接池最大连接数从10调整至50后缓解）。`,
        `瓶颈分析表明当前系统的性能制约因素主要集中在数据库I/O层面：复杂的关联查询（如帖子详情页需联合查询用户信息、分类信息、统计数据）执行耗时较长；未来可通过引入Redis缓存热点数据（如首页轮播图、热门帖子列表）、读写分离（主库负责写、从库负责读）、CDN加速静态资源等方式进一步提升吞吐能力。`,
        `小程序端的页面加载性能方面，使用微信开发者工具的Audits面板进行评测：首次冷启动加载耗时约1.8秒（含网络请求），页面切换动画流畅无明显掉帧（FPS稳定在55-60），分包加载策略（将二手交易、跑腿等低频功能拆分为独立分包）使得主包体积控制在2MB以内，符合小程序的体积限制要求。`
      ]
    },
    {
      title: "6.4 测试结论",
      paragraphs: [
        `经过全面的功能测试和性能测试，对本系统的质量状况得出以下结论：`,
        `功能性方面，本次测试覆盖了用户管理、论坛帖子、二手交易、失物招领、校园活动、互助跑腿、消息通知、内容审核、管理后台等全部九大模块的核心功能场景，共计编写26个测试用例，其中25个用例通过测试验证（通过率96.2%）。发现的1个缺陷为：在高并发抢单场景下偶发超卖现象（同一任务被分配给两人），根本原因是数据库乐观锁版本号校验的时序竞争问题，已通过在UPDATE SQL中加入AND version=?条件修复并回归通过。`,
        `性能方面，在预期的用户规模（日均活跃用户数百人级别）下，系统的响应速度、并发承载能力和资源消耗均处于健康范围内，不存在明显的性能短板。对于未来可能面临的更大流量冲击，已预先在架构设计中预留了缓存层、消息队列、水平扩展等优化手段的接入空间。`,
        `安全性方面，JWT认证机制有效防止了未授权的接口越权访问；敏感词过滤引擎能够拦截绝大多数常见的违规内容；密码哈希存储保证了即使数据库泄露也不会危及用户账户安全；CORS配置避免了跨域请求的滥用风险。但在生产环境中还需要增加接口限流（防刷）、验证码防机器人、HTTPS证书部署等措施进一步增强安全防护等级。`,
        `综上所述，本系统已经达到了预定的设计目标和质量标准，具备了上线试运行的条件。`
      ]
    }
  ]
};

const CHAPTER7 = {
  sections: [
    {
      title: "7.1 工作总结",
      paragraphs: [
        `本论文围绕"基于微信小程序的校园服务论坛系统的设计与实现"这一课题，开展了从需求调研、架构设计、编码实现到测试验证的完整软件开发周期工作，取得的研究成果和贡献主要体现在以下几个方面：`,
        `（1）完成了校园服务论坛系统的全栈开发交付。系统涵盖微信小程序用户端、Spring Boot后端API服务、Vue3管理后台三大子系统，实现了用户管理、论坛帖子、二手交易、失物招领、校园活动、互助跑腿、消息通知、信息聚合、内容审核九大功能模块，共包含22张数据库表、80余个API接口、30余个前端页面，形成了一套功能完整、可实际运行的软件产品。`,
        `（2）设计并实现了基于DFA算法的高性能敏感词过滤引擎。针对社区平台的内容安全痛点问题，自主研发了采用Trie树数据结构的敏感词检测方案，支持动态词库热加载和并发安全访问，实测检测性能达毫秒级响应，有效保障了社区内容的健康合规。`,
        `（3）构建了完善的身份认证与权限控制体系。集成Spring Security框架实现了JWT无状态认证和RBAC细粒度权限管理，配合AOP切面技术实现了操作日志的自动记录，形成了从前端路由守卫到后端方法级权限注解的多层次安全防线。`,
        `（4）积累了前后端分离架构的实践经验。通过本项目深入理解了RESTful API设计规范、前后端协作流程、版本控制和部署运维等工程化技能，提升了从需求到产品的全链路落地能力，为今后的职业发展打下了坚实的技术基础。`
      ]
    },
    {
      title: "7.2 不足与改进方向",
      paragraphs: [
        `尽管本系统已基本达到了预期目标，但受限于时间精力、技术水平和客观条件，仍然存在一些不足之处和可改进的方向：`,
        `（1）即时通讯功能的缺失。当前系统中的私信沟通采用的是简单的"留言板"模式（类似电商的阿里旺旺离线消息），缺乏真正的实时聊天能力。未来可考虑集成WebSocket长连接或第三方IM SDK（如腾讯云IM、环信），实现在线状态的实时显示、消息的即时推送已读回执等功能，提升用户之间的交互体验。`,
        `（2）支付功能的模拟性质。互助跑腿模块目前使用的是虚拟余额系统，未接入真实的第三方支付网关（微信支付/支付宝），这在一定程度上限制了商业闭环的形成。后续需完成商户资质申请和支付接口对接，实现真实的资金托管和清算分账流程。`,
        `（3）搜索功能的简陋。当前的搜索仅实现了对标题字段的关键词模糊匹配（LIKE '%keyword%'），缺乏分词、相关性排序、搜索建议等高级特性。未来可引入Elasticsearch搜索引擎实现全文检索，并结合用户行为数据进行个性化搜索结果排序。`,
        `（4）推荐算法的缺失。首页信息流目前按时间倒序简单排列，未能根据用户的兴趣偏好、历史行为、社交关系等因素进行千人千面的个性化内容推荐。后续可探索基于协同过滤或深度学习的推荐算法，提升用户粘性和内容消费时长。`,
        `（5）移动端适配的局限。管理后台仅针对PC端屏幕尺寸进行了布局优化，尚未开发适配手机浏览器或原生App的管理端入口，导致外出应急运维时不够便捷。可考虑基于uni-app开发跨平台的管理端小程序或使用响应式CSS媒体查询改造现有页面。`,
        `（6）测试覆盖率的不足。当前测试主要以手工黑盒功能测试为主，自动化单元测试和集成测试的覆盖率偏低，尤其是Service层的复杂业务逻辑缺少充分的边界条件和异常场景用例。未来应在开发过程中践行TDD（测试驱动开发）理念，持续积累测试资产，引入CI/CD流水线自动执行回归测试。`
      ]
    }
  ]
};

const REFERENCES = [
  "[1] 微信开放文档. 小程序开发指南[EB/OL]. https://developers.weixin.qq.com/miniprogram/dev/framework/, 2024.",
  "[2] Walls C, Craig R. Spring Boot in Action[M]. Manning Publications, 2016.",
  "[3] baomidou. MyBatis-Plus 官方文档[EB/OL]. https://baomidou.com/, 2024.",
  "[4] Oracle Corporation. MySQL 8.0 Reference Manual[EB/OL]. https://dev.mysql.com/doc/refman/8.0/en/, 2024.",
  "[5] Jones M, Bradley J, Sakimura N. JSON Web Token (JWT)[S]. RFC 7519, 2015.",
  "[6] Spring Security Reference Documentation[EB/OL]. https://docs.spring.io/spring-security/reference/, 2024.",
  "[7] 尤雨溪. Vue.js 3.0 官方文档[EB/OL]. https://cn.vuejs.org/, 2024.",
  "[8] Element Plus 团队. Element Plus 组件库文档[EB/OL]. https://element-plus.org/zh-CN/, 2024.",
  "[9] Aho A V, Corasick M J. Efficient String Matching: An Aid to Bibliographic Search[J]. Communications of the ACM, 1975, 18(6): 333-340.",
  "[10] Fielding R T. Architectural Styles and the Design of Network-based Software Architectures[D]. University of California, Irvine, 2000.",
  "[11] 中国互联网络信息中心. 第53次中国互联网络发展状况统计报告[R]. CNNIC, 2024.",
  "[12] 李刚. Spring Boot 2.x精髓：从构建小系统到架构分布式大系统[M]. 电子工业出版社, 2019.",
  "[13] 程杰. 大话数据结构[M]. 清华大学出版社, 2011.",
  "[14] Redmond K, Wilson J. RESTful Web APIs[M]. O'Reilly Media, 2013.",
  "[15] 腾讯云. 微信小程序云开发技术文档[EB/OL]. https://cloud.tencent.com/document/product/876/, 2024."
];

const ACKNOWLEDGEMENT = `时光荏苒，岁月如梭，四年的大学生活即将画上句号。在毕业论文完成之际，我要向所有给予我帮助和支持的人表达最诚挚的感谢。

首先，我要衷心感谢我的指导老师。从选题方向的确定到技术方案的选择，从系统架构的设计到论文撰写的润色，老师始终给予了悉心的指导和耐心的解答。老师严谨的治学态度、渊博的专业知识和认真负责的工作作风深深影响了我，使我受益匪浅。

其次，我要感谢计算机学院的各位任课教师。正是您们在课堂上传授的数据结构与算法、软件工程、数据库原理、计算机网络等专业课程知识，为我完成本项目打下了坚实的理论基础。

感谢我的同学们和实验室的伙伴们。在项目开发过程中，我们一起讨论技术难题、分享学习经验、互相鼓励支持。特别是室友们在我熬夜调试bug时送来的关怀，让我倍感温暖。

感谢我的家人。你们一直以来对我的理解、支持和无私付出，是我不断前行的动力源泉。没有你们的默默付出，我不可能顺利完成学业。

最后，感谢所有为本项目提供帮助的工具和框架的开源社区 contributors，正是站在巨人的肩膀上，我才得以完成这个系统。

路漫漫其修远兮，吾将上下而求索。在未来的学习和工作中，我将继续保持求知若渴的态度，不断提升自己的技术能力和综合素质，努力成为一名合格的软件工程师，回报所有关心和帮助过我的人。`;

// ==================== 文档生成 ====================

function createDocument() {
  const border = { style: BorderStyle.SINGLE, size: 1, color: "000000" };
  const borders = { top: border, bottom: border, left: border, right: border };
  
  // 创建段落辅助函数
  function p(text, options = {}) {
    return new Paragraph({
      alignment: options.align || AlignmentType.JUSTIFIED,
      spacing: { after: 200, line: 360 },
      indent: options.indent ? { firstLine: 480 } : undefined,
      ...options.extra,
      children: [new TextRun({ text, size: 24, font: "宋体", options: { bold: options.bold } })]
    });
  }

  function heading1(text) {
    return new Paragraph({
      heading: HeadingLevel.HEADING_1,
      alignment: AlignmentType.CENTER,
      spacing: { before: 400, after: 300 },
      children: [new TextRun({ text, size: 32, bold: true, font: "黑体" })]
    });
  }

  function heading2(text) {
    return new Paragraph({
      heading: HeadingLevel.HEADING_2,
      spacing: { before: 300, after: 200 },
      children: [new TextRun({ text, size: 28, bold: true, font: "黑体" })]
    });
  }

  function heading3(text) {
    return new Paragraph({
      spacing: { before: 200, after: 150 },
      children: [new TextRun({ text, size: 26, bold: true, font: "楷体" })]
    });
  }

  // 构建所有子章节内容
  function buildSections(chapter) {
    const children = [];
    for (const section of chapter.sections) {
      children.push(heading2(section.title));
      if (section.paragraphs) {
        for (const para of section.paragraphs) {
          children.push(p(para, { indent: true }));
        }
      }
      if (section.subModules) {
        for (const mod of section.subModules) {
          children.push(new Paragraph({
            spacing: { after: 150, line: 360 },
            indent: { firstLine: 480 },
            children: [
              new TextRun({ text: mod.name, size: 24, bold: true, font: "宋体" })
            ]
          }));
          children.push(p(mod.desc, { indent: true }));
        }
      }
      if (section.techStack) {
        children.push(createTechStackTable(section.techStack));
      }
      if (section.testCases) {
        children.push(createTestCasesTable(section.testCases));
      }
      if (section.dbDiagram) {
        children.push(...createDbStructureDiagram());
      }
    }
    return children;
  }

  function createDbStructureDiagram() {
    const boxBorder = { style: BorderStyle.SINGLE, size: 1, color: "666666" };
    const boxBorders = { top: boxBorder, bottom: boxBorder, left: boxBorder, right: boxBorder };

    const diagramRows = [
      ["user", "post", "comment", "message"],
      ["product", "order_master", "help_task", "help_order"],
      ["activity", "registration", "lost_found", "banner"]
    ];

    const rows = diagramRows.map(row => new TableRow({
      children: row.map(cell => new TableCell({
        borders: boxBorders,
        width: { size: 2256, type: WidthType.DXA },
        margins: { top: 120, bottom: 120, left: 100, right: 100 },
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [new TextRun({ text: cell, size: 22, bold: true, font: "Times New Roman" })]
        })]
      }))
    }));

    return [
      new Paragraph({
        spacing: { before: 120, after: 120, line: 360 },
        indent: { firstLine: 480 },
        children: [new TextRun({ text: "图4-1展示了本系统核心业务数据表之间的结构分组关系。", size: 24, font: "宋体" })]
      }),
      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [2256, 2256, 2256, 2258],
        rows
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 120, after: 220 },
        children: [new TextRun({ text: "图4-1 数据库结构图（核心实体关系分组示意）", size: 22, font: "宋体" })]
      }),
      new Paragraph({
        spacing: { after: 180, line: 360 },
        indent: { firstLine: 480 },
        children: [
          new TextRun({ text: "关系说明：", size: 24, bold: true, font: "宋体" }),
          new TextRun({ text: "user→post（1:N），post→comment（1:N），product→order_master（1:N），help_task→help_order（1:N），activity→registration（1:N）。", size: 24, font: "宋体" })
        ]
      })
    ];
  }

  function createTechStackTable(data) {
    const rows = data.map((row, idx) => {
      return new TableRow({
        children: row.map(cell => new TableCell({
          borders,
          width: { size: Math.floor(9026 / row.length), type: WidthType.DXA },
          shading: idx === 0 ? { fill: "E8E8E8", type: ShadingType.CLEAR } : undefined,
          margins: { top: 60, bottom: 60, left: 100, right: 100 },
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ text: cell, size: 21, font: "宋体", bold: idx === 0 })]
          })]
        }))
      });
    });

    return new Table({
      width: { size: 9026, type: WidthType.DXA },
      columnWidths: data[0].map(() => Math.floor(9026 / data[0].length)),
      rows
    });
  }

  function createTestCasesTable(cases) {
    const headers = ["测试模块", "测试用例", "预期/实际结果"];
    const rows = [
      new TableRow({
        children: headers.map(h => new TableCell({
          borders,
          width: { size: h === "测试模块" ? 1800 : h === "测试用例" ? 3500 : 3726, type: WidthType.DXA },
          shading: { fill: "E8E8E8", type: ShadingType.CLEAR },
          margins: { top: 60, bottom: 60, left: 100, right: 100 },
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ text: h, size: 21, font: "宋体", bold: true })]
          })]
        }))
      }),
      ...cases.map(tc => new TableRow({
        children: [tc.module, tc.case, tc.result].map((cell, ci) => new TableCell({
          borders,
          width: { size: ci === 0 ? 1800 : ci === 1 ? 3500 : 3726, type: WidthType.DXA },
          margins: { top: 60, bottom: 60, left: 100, right: 100 },
          children: [new Paragraph({
            children: [new TextRun({ text: cell, size: 21, font: "宋体" })]
          })]
        }))
      }))
    ];

    return new Table({
      width: { size: 9026, type: WidthType.DXA },
      columnWidths: [1800, 3500, 3726],
      rows
    });
  }

  function createManualToc() {
    const items = [
      "摘  要",
      "Abstract",
      "第一章  绪论",
      "第二章  相关技术介绍",
      "第三章  系统需求分析",
      "第四章  系统总体设计",
      "第五章  详细设计与实现",
      "第六章  系统测试",
      "第七章  总结与展望",
      "致  谢",
      "参考文献"
    ];

    return items.map(text => new Paragraph({
      spacing: { after: 120, line: 360 },
      children: [new TextRun({ text, size: 24, font: "宋体" })]
    }));
  }

  // ===== 构建文档 =====
  const allChildren = [];

  // ---- 封面 ----
  allChildren.push(new Paragraph({ spacing: { after: 1200 }, children: [] }));
  allChildren.push(new Paragraph({ spacing: { after: 800 }, children: [] }));
  allChildren.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 600 },
    children: [new TextRun({ text: "毕业设计（论文）", size: 44, bold: true, font: "黑体" })]
  }));
  allChildren.push(new Paragraph({ spacing: { after: 600 }, children: [] }));
  allChildren.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 400 },
    children: [new TextRun({ text: THESIS_TITLE, size: 36, bold: true, font: "黑体" })]
  }));
  allChildren.push(new Paragraph({ spacing: { after: 1600 }, children: [] }));
  allChildren.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [new TextRun({ text: "所在院系：计算机学院", size: 28, font: "宋体" })]
  }));
  allChildren.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [new TextRun({ text: "专业班级：软件工程", size: 28, font: "宋体" })]
  }));
  allChildren.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [new TextRun({ text: "学生姓名：_______________", size: 28, font: "宋体" })]
  }));
  allChildren.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [new TextRun({ text: "指导教师：_______________", size: 28, font: "宋体" })]
  }));
  allChildren.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [new TextRun({ text: "二〇二五年六月", size: 28, font: "宋体" })]
  }));

  // ---- 分页：摘要 ----
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("摘  要"));
  allChildren.push(p(ABSTRACT_CN, { indent: true }));
  allChildren.push(new Paragraph({
    spacing: { before: 300, after: 200, line: 360 },
    indent: { firstLine: 480 },
    children: [
      new TextRun({ text: "关键词：", size: 24, bold: true, font: "宋体" }),
      new TextRun({ text: KEYWORDS.join("；"), size: 24, font: "宋体" })
    ]
  }));

  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("Abstract"));
  allChildren.push(p(ABSTRACT_EN, { indent: true }));
  allChildren.push(new Paragraph({
    spacing: { before: 300, after: 200, line: 360 },
    children: [
      new TextRun({ text: "Keywords: ", size: 24, bold: true, font: "Times New Roman" }),
      new TextRun({ text: KEYWORDS_EN.join("; "), size: 24, font: "Times New Roman" })
    ]
  }));

  // ---- 目录 ----
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("目  录"));
  allChildren.push(...createManualToc());
  allChildren.push(new Paragraph({ spacing: { after: 120 }, children: [] }));
  allChildren.push(new TableOfContents("目录", { hyperlink: true, headingStyleRange: "1-3" }));

  // ---- 正文 ----
  // 第一章
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("第一章  绪论"));
  allChildren.push(...buildSections(CHAPTER1));

  // 第二章
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("第二章  相关技术介绍"));
  allChildren.push(...buildSections(CHAPTER2));

  // 第三章
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("第三章  系统需求分析"));
  allChildren.push(...buildSections(CHAPTER3));

  // 第四章
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("第四章  系统总体设计"));
  allChildren.push(...buildSections(CHAPTER4));

  // 第五章
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("第五章  详细设计与实现"));
  allChildren.push(...buildSections(CHAPTER5));

  // 第六章
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("第六章  系统测试"));
  allChildren.push(...buildSections(CHAPTER6));

  // 第七章
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("第七章  总结与展望"));
  allChildren.push(...buildSections(CHAPTER7));

  // ---- 致谢 ----
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("致  谢"));
  const thanksParas = ACKNOWLEDGEMENT.split('\n\n').map(para => p(para, { indent: true }));
  allChildren.push(...thanksParas);

  // ---- 参考文献 ----
  allChildren.push(new Paragraph({ children: [new PageBreak()] }));
  allChildren.push(heading1("参考文献"));
  for (const ref of REFERENCES) {
    allChildren.push(new Paragraph({
      spacing: { after: 120, line: 360 },
      children: [new TextRun({ text: ref, size: 22, font: "宋体" })]
    }));
  }

  // 创建文档
  const doc = new Document({
    styles: {
      default: {
        document: {
          run: { font: "宋体", size: 24 }
        }
      },
      paragraphStyles: [
        {
          id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { size: 32, bold: true, font: "黑体" },
          paragraph: { spacing: { before: 400, after: 300 }, outlineLevel: 0 }
        },
        {
          id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { size: 28, bold: true, font: "黑体" },
          paragraph: { spacing: { before: 300, after: 200 }, outlineLevel: 1 }
        },
        {
          id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
          run: { size: 26, bold: true, font: "楷体" },
          paragraph: { spacing: { before: 200, after: 150 }, outlineLevel: 2 }
        }
      ]
    },
    numbering: {
      config: [{
        reference: "bullets",
        levels: [{
          level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } }
        }]
      }]
    },
    sections: [{
      properties: {
        page: {
          size: { width: 11906, height: 16838 }, // A4
          margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
        }
      },
      headers: {
        default: new Header({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [new TextRun({ text: THESIS_TITLE, size: 18, font: "宋体", color: "888888" })]
          })]
        })
      },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
              new TextRun({ text: "第 ", size: 20, font: "宋体" }),
              new TextRun({ children: [PageNumber.CURRENT], size: 20, font: "宋体" }),
              new TextRun({ text: " 页", size: 20, font: "宋体" })
            ]
          })]
        })
      },
      children: allChildren
    }]
  });

  return doc;
}

// 执行生成
async function main() {
  console.log("开始生成论文...");
  const doc = createDocument();
  const buffer = await Packer.toBuffer(doc);
  const outputPath = 'D:\\graduationProject\\campus-forum\\毕业论文_基于微信小程序的校园服务论坛系统的设计与实现.docx';
  fs.writeFileSync(outputPath, buffer);
  console.log(`论文生成成功！\n文件路径: ${outputPath}\n文件大小: ${(buffer.length / 1024).toFixed(1)} KB`);
}

main().catch(console.error);
