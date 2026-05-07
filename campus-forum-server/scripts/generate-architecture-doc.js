const fs = require('fs');
const path = require('path');
const {
  AlignmentType,
  BorderStyle,
  Document,
  Packer,
  PageOrientation,
  Paragraph,
  ShadingType,
  Table,
  TableCell,
  TableRow,
  TextRun,
  VerticalAlign,
  WidthType,
} = require('docx');

const outDir = path.resolve(__dirname, '../../output');
const outPath = path.join(outDir, 'campus-forum-system-architecture.docx');

if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

const tableWidth = 14400;
const cellWidth = 3600;
const border = { style: BorderStyle.SINGLE, size: 2, color: 'CBD5E1' };

function cell(title, desc, fill) {
  return new TableCell({
    width: { size: cellWidth, type: WidthType.DXA },
    verticalAlign: VerticalAlign.CENTER,
    shading: { fill, type: ShadingType.CLEAR },
    borders: { top: border, bottom: border, left: border, right: border },
    margins: { top: 120, bottom: 120, left: 120, right: 120 },
    children: [
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { after: 80 },
        children: [new TextRun({ text: title, bold: true, size: 24 })],
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: desc, size: 18, color: '334155' })],
      }),
    ],
  });
}

function arrowCell(label) {
  return new TableCell({
    width: { size: cellWidth, type: WidthType.DXA },
    verticalAlign: VerticalAlign.CENTER,
    borders: { top: border, bottom: border, left: border, right: border },
    margins: { top: 50, bottom: 50, left: 120, right: 120 },
    children: [
      new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: label, bold: true, size: 22, color: '334155' })],
      }),
    ],
  });
}

function makeLayerTable(items, fills) {
  return new Table({
    width: { size: tableWidth, type: WidthType.DXA },
    columnWidths: [cellWidth, cellWidth, cellWidth, cellWidth],
    rows: [
      new TableRow({
        children: items.map((item, index) => cell(item.title, item.desc, fills[index])),
      }),
      new TableRow({
        children: [arrowCell('⇩'), arrowCell('⇩'), arrowCell('⇩'), arrowCell('⇩')],
      }),
    ],
  });
}

const clientItems = [
  { title: '微信小程序端', desc: '论坛 / 商品 / 互助 / 消息 / 个人中心' },
  { title: '管理后台', desc: '内容审核 / 用户管理 / 商品与系统管理' },
  { title: 'Web 管理接口', desc: 'Vue3 + Vite + API 代理' },
  { title: '统一入口', desc: '统一 API 前缀 /api' },
];

const serviceItems = [
  { title: 'Spring Boot API', desc: 'REST 接口 / 参数校验 / 统一返回' },
  { title: 'JWT 安全层', desc: '登录认证 / 权限控制 / Token 解析' },
  { title: '业务服务层', desc: '论坛 / 服务交易 / 用户中心' },
  { title: '消息与搜索', desc: '通知 / IM / Elasticsearch' },
];

const infraItems = [
  { title: 'MySQL', desc: '核心业务数据持久化' },
  { title: 'Redis', desc: '验证码 / 会话 / 缓存' },
  { title: 'Elasticsearch', desc: '全文检索 / 搜索推荐' },
  { title: '文件存储', desc: '头像 / 图片 / 附件' },
];

const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: 'Arial', size: 22 },
      },
    },
  },
  sections: [
    {
      properties: {
        page: {
          size: {
            width: 12240,
            height: 15840,
            orientation: PageOrientation.LANDSCAPE,
          },
          margin: { top: 720, right: 720, bottom: 720, left: 720 },
        },
      },
      children: [
        new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [new TextRun({ text: '校园论坛系统架构图', bold: true, size: 34 })],
        }),
        new Paragraph({
          alignment: AlignmentType.CENTER,
          spacing: { after: 220 },
          children: [
            new TextRun({
              text: '本图展示微信小程序、管理后台、Spring Boot 服务端及基础设施的分层关系',
              italics: true,
              size: 20,
              color: '475569',
            }),
          ],
        }),
        new Paragraph({
          spacing: { after: 120 },
          children: [new TextRun({ text: '1. 客户端层', bold: true, size: 24, color: '0f172a' })],
        }),
        makeLayerTable(clientItems, ['DBEAFE', 'E0E7FF', 'DCFCE7', 'FEF3C7']),
        new Paragraph({
          spacing: { before: 180, after: 120 },
          children: [new TextRun({ text: '2. 接入与业务层', bold: true, size: 24, color: '0f172a' })],
        }),
        makeLayerTable(serviceItems, ['FDE68A', 'FCE7F3', 'BBF7D0', 'E0F2FE']),
        new Paragraph({
          spacing: { before: 180, after: 120 },
          children: [new TextRun({ text: '3. 基础设施层', bold: true, size: 24, color: '0f172a' })],
        }),
        makeLayerTable(infraItems, ['DDD6FE', 'E2E8F0', 'F3E8FF', 'FFE4E6']),
        new Paragraph({
          spacing: { before: 220 },
          children: [
            new TextRun({
              text: '说明：客户端统一通过 Spring Boot REST API 访问业务服务，核心数据落 MySQL，搜索走 Elasticsearch，临时状态与会话由 Redis 支撑。',
              size: 18,
              color: '475569',
            }),
          ],
        }),
      ],
    },
  ],
});

Packer.toBuffer(doc).then((buffer) => {
  fs.writeFileSync(outPath, buffer);
  console.log(`Wrote ${outPath}`);
});
