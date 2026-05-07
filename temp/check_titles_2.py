import docx
path = r'D:/graduationProject/campus-forum/论文结构参考_业务域重构.docx'
doc = docx.Document(path)
print('--- Target Paragraphs ---')
for p in doc.paragraphs:
    t = p.text.strip()
    if t.startswith('第') and '章' in t:
         print(t)
    elif t.startswith('4 ') or t.startswith('6 ') or t.startswith('7 '):
         print(t)
