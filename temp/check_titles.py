import docx
path = r'D:/graduationProject/campus-forum/论文结构参考_业务域重构.docx'
doc = docx.Document(path)
print('--- Detailed Check ---')
for p in doc.paragraphs:
    t = p.text.strip()
    if any(c in t for c in ['第4章', '第6章', '第7章', '4 ', '6 ', '7 ']):
        print(f'Match: {t}')
