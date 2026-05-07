import docx
path = r'D:/graduationProject/campus-forum/论文结构参考_业务域重构.docx'
doc = docx.Document(path)
print('--- All Paragraphs Start ---')
for p in doc.paragraphs:
    if p.text.strip():
        print(p.text.strip())
print('--- All Paragraphs End ---')
