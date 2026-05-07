import docx
path = r'D:/graduationProject/campus-forum/论文结构参考_业务域重构.docx'
doc = docx.Document(path)
for p in doc.paragraphs:
    t = p.text.strip()
    # Check if starts with digit followed by dot
    if len(t) > 2 and t[0].isdigit() and t[1] == '.':
         if t.startswith('4.') or t.startswith('6.') or t.startswith('7.'):
             print(f'Section: {t}')
