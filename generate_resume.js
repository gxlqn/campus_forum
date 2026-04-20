const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, BorderStyle, WidthType, ShadingType, VerticalAlign,
  HeadingLevel, PageNumber
} = require('docx');
const fs = require('fs');

// ========== 简历内容 ==========
const RESUME = {
  name: "张三",
  target: "软件测试工程师",
  phone: "138-xxxx-xxxx",
  email: "zhangsan@example.com",
  location: "XX省XX市",
  education: {
    school: "XX大学",
    major: "软件工程",
    degree: "本科",
    time: "2021.09 - 2025.06"
  },
  skills: [
    "熟悉软件测试理论和方法，掌握黑盒测试、白盒测试、回归测试、性能测试等测试类型",
    "熟练使用 Postman 进行接口测试，能够设计接口测试用例并验证响应数据的正确性",
    "掌握 Apache JMeter 性能测试工具，能进行并发压测、TPS/QPS 指标分析和瓶颈定位",
    "熟悉 MySQL 数据库操作，能编写 SQL 查询语句进行数据验证和数据准备",
    "了解 CI/CD 流程及自动化测试框架（JUnit、Spring Boot Test），有单元测试编写经验",
    "熟悉 JWT 认证机制、Spring Security 权限控制原理，能进行安全性相关测试",
    "掌握微信小程序调试工具和 Chrome DevTools，具备前后端联调测试能力",
    "熟悉 Linux 常用命令，能在服务器环境部署应用并进行日志排查与问题定位",
    "具有良好的文档撰写能力，能独立编写测试用例、测试报告和缺陷报告（Bug Report）",
    "了解 DFA 敏感词过滤算法原理，有内容安全审核类功能的测试经验"
  ],
  project: {
    name: "基于微信小程序的校园服务论坛系统",
    role: "测试工程师 / 全栈开发",
    time: "2024.10 - 2025.04",
    desc: "一套面向高校师生的综合性校园服务平台，涵盖微信小程序用户端、Spring Boot后端服务、Vue3管理后台三大子系统，包含用户管理、论坛帖子、二手交易、失物招领、校园活动、互助跑腿、消息通知、内容审核等9大功能模块，共22张数据库表、80+ API接口。",
    duties: [
      {
        title: "测试计划与用例设计",
        content: "根据需求规格说明书制定系统测试计划，覆盖功能测试、性能测试和安全测试三大类别；采用等价类划分、边界值分析、场景法等方法设计测试用例共计26个核心场景+50+扩展场景，编写测试用例文档并组织评审。"
      },
      {
        title: "功能测试执行与管理",
        content: "对9大业务模块进行全面黑盒测试：包括用户微信授权登录流程（code换取openid→JWT签发→身份绑定）、帖子发布/编辑/删除/点赞/收藏全链路、二手商品上架/下架/状态流转、互助跑腿抢单并发竞争、消息通知触发与推送等。使用Postman对80余个RESTful接口逐一验证请求参数校验、响应格式规范性和业务逻辑正确性，发现并跟踪修复缺陷15个，最终通过率达96.2%。"
      },
      {
        title: "接口测试与安全验证",
        content: "针对JWT认证机制设计专项测试：验证Token过期拒绝访问(401)、无权限资源访问(403)、伪造Token拦截、SQL注入防护（MyBatis-Plus参数绑定验证）、XSS跨站脚本攻击检测、文件上传漏洞扫描（非法扩展名/超大文件/恶意文件头）等安全场景，确认Spring Security过滤器链配置的有效性。"
      },
      {
        title: "性能测试与瓶颈分析",
        content: "使用Apache JMeter搭建压测环境，设计线程组（50→100→200并发用户递进）、循环次数（10次/线程）、Ramp-up时间(5s)等参数方案，对登录、发帖、抢单、浏览列表等核心接口执行压力测试。分析结果：100并发下平均响应<150ms、错误率0%、吞吐量~300 QPS；200并发时数据库连接池出现等待超时瓶颈，通过调整连接数从10→50优化解决。输出性能测试报告含响应时间趋势图和优化建议。"
      },
      {
        title: "敏感词过滤引擎专项测试",
        content: "针对自研DFA（确定性有限自动机）敏感词检测引擎设计专项测试：构建5000条敏感词词库，编写正常文本/纯敏感词/混合嵌入/边界变体/Unicode绕过等30+测试样本，验证检测准确率100%和误报率<0.1%；通过JMeter压测验证5000词库条件下单次1000字文本检测耗时<1ms的性能指标满足线上实时检测要求。"
      },
      {
        title: "兼容性与用户体验测试",
        content: "在微信开发者工具(iOS/Android模拟器)和真机上验证小程序页面渲染一致性，使用Audits面板评测冷启动加载速度(~1.8s)、FPS稳定性(55-60)、分包体积合规(<2MB)；在Chrome/Firefox/Edge多浏览器中验证管理后台Element Plus组件布局适配和交互流畅度。"
      },
      {
        title: "缺陷管理与质量改进",
        content: "建立缺陷分级标准(P0崩溃/P1阻塞/P2一般/P3优化)，使用Excel缺陷追踪模板记录每个Bug的重现步骤、预期结果、实际结果和截图证据；推动开发团队修复关键缺陷（如高并发抢单超卖问题——根本原因数据库乐观锁版本号竞争，已通过UPDATE加version条件修复并回归通过）；输出《系统测试总结报告》作为验收交付物。"
      }
    ],
    techStack: ["微信小程序", "Spring Boot", "MySQL", "Postman", "JMeter", "Chrome DevTools", "JWT/Spring Security", "Git"]
  },
  experience: [
    { company: "XX科技有限公司", role: "软件测试实习生", time: "2024.07 - 2024.09", content: "参与公司电商平台的迭代版本测试，负责购物车模块和支付流程的功能测试；使用禅道/JIRA管理缺陷生命周期，累计提交有效Bug报告40+个。" },
    { company: "XX大学软件学院", role: "课程项目组长", time: "2023.03 - 2023.06", content: "带领5人团队完成图书管理系统开发与测试，负责整体测试策略制定和核心模块测试执行，获得课程设计优秀评级。" }
  ],
  certifications: ["软件设计师（中级）—— 中国计算机技术职业资格网", "CET-6 英语六级（520分）—— 具备英文测试文档阅读能力"],
  selfEval: "热爱软件测试工作，注重细节和质量意识强；具备良好的逻辑思维能力和问题分析能力，善于从用户角度发现潜在缺陷；有全栈开发经验（Java/Vue/小程序），能够理解代码逻辑从而更精准地定位Bug根因；学习能力强，乐于接受新技术和新工具，团队协作沟通顺畅。"
};

// ========== 文档生成 ==========
function createResume() {
  const noBorder = { style: BorderStyle.NIL, size: 0, color: "FFFFFF" };
  const lightBorder = (color = "DDDDDD") => ({ style: BorderStyle.SINGLE, size: 1, color });
  const thinBorders = { top: lightBorder(), bottom: lightBorder(), left: noBorder, right: noBorder };

  function h1(text) {
    return new Paragraph({
      alignment: AlignmentType.LEFT,
      spacing: { before: 300, after: 150 },
      border: { bottom: { style: BorderStyle.SINGLE, size: 12, color: "2B579A" } },
      children: [new TextRun({ text, size: 26, bold: true, font: "黑体", color: "2B579A" })]
    });
  }

  function p(text, opts = {}) {
    return new Paragraph({
      alignment: opts.align || AlignmentType.JUSTIFIED,
      spacing: { after: opts.after || 120, line: opts.line || 276 },
      indent: opts.indent ? { firstLine: 360 } : undefined,
      children: [new TextRun({ text, size: 21, font: "宋体", ...opts.run })]
    });
  }

  function boldLabel(label, value) {
    return new Paragraph({
      spacing: { after: 80, line: 260 },
      children: [
        new TextRun({ text: label, size: 21, bold: true, font: "宋体" }),
        new TextRun({ text: value, size: 21, font: "宋体" })
      ]
    });
  }

  // 技能表格行
  function skillTableRows() {
    return RESUME.skills.map((skill, i) => 
      new TableRow({
        children: [
          new TableCell({
            borders: thinBorders,
            width: { size: 400, type: WidthType.DXA },
            margins: { top: 40, bottom: 40, left: 60, right: 60 },
            verticalAlign: VerticalAlign.CENTER,
            children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: String(i + 1), size: 18, font: "Arial", color: "2B579A", bold: true })] })]
          }),
          new TableCell({
            borders: thinBorders,
            width: { size: 8626, type: WidthType.DXA },
            margins: { top: 40, bottom: 40, left: 120, right: 60 },
            children: [new Paragraph({ children: [new TextRun({ text: skill, size: 20, font: "宋体" })] })]
          })
        ]
      })
    );
  }

  // 项目职责表格行
  function dutyRows() {
    return RESUME.project.duties.map((duty, i) =>
      new TableRow({
        children: [
          new TableCell({
            borders: { top: noBorder, bottom: i < RESUME.project.duties.length - 1 ? lightBorder("EEEEEE") : noBorder, left: noBorder, right: noBorder },
            width: { size: 9026, type: WidthType.DXA },
            margins: { top: 120, bottom: 120, left: 0, right: 0 },
            children: [
              new Paragraph({ spacing: { after: 60 }, children: [new TextRun({ text: duty.title, size: 21, bold: true, font: "宋体", color: "333333" })] }),
              new Paragraph({ spacing: { after: 0, line: 276 }, children: [new TextRun({ text: duty.content, size: 20, font: "宋体", color: "555555" })] })
            ]
          })
        ]
      })
    );
  }

  const doc = new Document({
    styles: {
      default: { document: { run: { font: "宋体", size: 21 } } }
    },
    sections: [{
      properties: {
        page: {
          size: { width: 11906, height: 16838 },
          margin: { top: 720, right: 720, bottom: 720, left: 720 }
        }
      },
      children: [
        // ====== 姓名 + 求职意向 ======
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { after: 80 },
          children: [new TextRun({ text: RESUME.name, size: 44, bold: true, font: "黑体", color: "1a1a1a" })]
        }),
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { after: 240 },
          children: [new TextRun({ text: RESUME.target, size: 24, font: "楷体", color: "2B579A" })]
        }),

        // ====== 基本信息 ======
        new Table({
          width: { size: 9026, type: WidthType.DXA },
          columnWidths: [2257, 2256, 2256, 2257],
          rows: [new TableRow({
            children: [
              ["\u260E " + RESUME.phone, "\u2709 " + RESUME.email, "\uF041 " + RESUME.location, "\u25CE " + RESUME.education.degree].map(t =>
                new TableCell({
                  borders: { top: noBorder, bottom: noBorder, left: noBorder, right: noBorder },
                  width: { size: 2256, type: WidthType.DXA },
                  children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: t, size: 19, font: "宋体", color: "555555" })] })]
                })
              )
            ]
          })]
        }),

        // ====== 教育背景 ======
        h1("\u25A0  \u6559\u80B2\u80CC\u666F"),
        new Table({
          width: { size: 9026, type: WidthType.DXA },
          columnWidths: [3000, 3026, 3000],
          rows: [new TableRow({
            children: [
              [RESUME.education.school, RESUME.education.major + " | " + RESUME.education.degree, RESUME.education.time].map(t =>
                new TableCell({
                  borders: { top: noBorder, bottom: noBorder, left: noBorder, right: noBorder },
                  width: { size: 3008, type: WidthType.DXA },
                  children: [new Paragraph({ children: [new TextRun({ text: t, size: 21, font: "宋体" })] })]
                })
              )
            ]
          })]
        }),

        // ====== 专业技能 ======
        h1("\u25A0  \u4E13\u4E1A\u6280\u80FD"),
        new Table({
          width: { size: 9026, type: WidthType.DXA },
          columnWidths: [400, 8626],
          rows: skillTableRows()
        }),

        // ====== 项目经历 ======
        h1("\u25A0  \u9879\u76EE\u7ECF\u5386"),
        boldLabel("项目名称：", RESUME.project.name),
        boldLabel("担任角色：", RESUME.project.role + "　　" + "项目时间：" + RESUME.project.time),
        p("项目描述：" + RESUME.project.desc, { indent: true, after: 160 }),
        new Paragraph({
          spacing: { before: 80, after: 40 },
          children: [new TextRun({ text: "主要职责与产出：", size: 21, bold: true, font: "宋体", color: "333333" })]
        }),
        new Table({
          width: { size: 9026, type: WidthType.DXA },
          columnWidths: [9026],
          rows: dutyRows()
        }),
        new Paragraph({
          spacing: { before: 100, after: 120 },
          children: [
            new TextRun({ text: "技术栈：", size: 20, bold: true, font: "宋体", color: "2B579A" }),
            new TextRun({ text: RESUME.project.techStack.join(" / "), size: 20, font: "宋体", color: "666666" })
          ]
        }),

        // ====== 实习/实践经历 ======
        h1("\u25A0  \u5B9E\u4E60/\u5B9E\u8DF5\u7ECF\u5386"),
        ...RESUME.experience.flatMap(exp => [
          new Table({
            width: { size: 9026, type: WidthType.DXA },
            columnWidths: [4500, 2000, 2526],
            rows: [new TableRow({
              children: [
                new TableCell({ borders: { top: noBorder, bottom: noBorder, left: noBorder, right: noBorder }, width: { size: 4500, type: WidthType.DXA }, children: [new Paragraph({ children: [new TextRun({ text: exp.company, size: 21, bold: true, font: "宋体" })] })] }),
                new TableCell({ borders: { top: noBorder, bottom: noBorder, left: noBorder, right: noBorder }, width: { size: 2000, type: WidthType.DXA }, children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: exp.role, size: 20, font: "宋体", color: "555555" })] })] }),
                new TableCell({ borders: { top: noBorder, bottom: noBorder, left: noBorder, right: noBorder }, width: { size: 2526, type: WidthType.DXA }, children: [new Paragraph({ alignment: AlignmentType.RIGHT, children: [new TextRun({ text: exp.time, size: 19, font: "宋体", color: "888888" })] })] })
              ]
            })]
          }),
          p(exp.content, { indent: true, after: 180 })
        ]),

        // ====== 资格证书 ======
        h1("\u25A0  \u8D44\u683C\u8BC1\u4E66"),
        ...RESUME.certifications.map(cert => p(cert)),

        // ====== 自我评价 ======
        h1("\u25A0  \u81EA\u6211\u8BC4\u4EF7"),
        p(RESUME.selfEval, { indent: true, line: 280 })
      ]
    }]
  });

  return doc;
}

async function main() {
  console.log("正在生成简历...");
  const doc = createResume();
  const buffer = await Packer.toBuffer(doc);
  const path = 'D:\\graduationProject\\campus-forum\\简历_软件测试工程师.docx';
  fs.writeFileSync(path, buffer);
  console.log("简历生成成功！");
  console.log("路径: " + path);
  console.log("大小: " + (buffer.length / 1024).toFixed(1) + " KB");
}

main().catch(console.error);
