import docx
path = r'D:/graduationProject/campus-forum/论文结构参考_业务域重构.docx'
doc = docx.Document(path)
for p in doc.paragraphs:
    t = p.text.strip()
    # Looking for lines like "第4章 系统总体设计"
    if t.startswith('第4章') or t.startswith('第6章') or t.startswith('第7章'):
        print(f'Header Found: {t}')
    # Also check if it's just the number usually sometimes there's a space or style
    if t == '第4章' or t == '第6章' or t == '第7章':
        print(f'Header Found (exact): {t}')
