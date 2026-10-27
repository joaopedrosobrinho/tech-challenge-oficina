"""Gera o PDF da entrega. Sem --video-url, produz uma prévia identificada."""
import argparse
from pathlib import Path
from xml.sax.saxutils import escape
from urllib.parse import urlparse
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon

parser = argparse.ArgumentParser()
parser.add_argument('--video-url')
parser.add_argument('--output', default='target/entrega-fase-2-previa.pdf')
args = parser.parse_args()
if args.video_url:
    url = urlparse(args.video_url)
    if url.scheme != 'https' or url.hostname not in {'youtube.com', 'www.youtube.com', 'youtu.be', 'vimeo.com', 'www.vimeo.com'}:
        raise SystemExit('Informe um link HTTPS do YouTube ou Vimeo.')
root = Path(__file__).resolve().parents[1]
destination = root / args.output
destination.parent.mkdir(parents=True, exist_ok=True)
styles = getSampleStyleSheet()
styles.add(ParagraphStyle('Heading', parent=styles['Heading1'], fontSize=20, leading=25, textColor=colors.HexColor('#17324d'), spaceAfter=16))
styles.add(ParagraphStyle('Text', parent=styles['BodyText'], fontSize=10.5, leading=16, spaceAfter=10))
styles.add(ParagraphStyle('SmallCenter', parent=styles['BodyText'], fontSize=9, leading=13, alignment=TA_CENTER))
story = []


def paragraph(text, style='Text'):
    story.append(Paragraph(text, styles[style]))


def diagram():
    drawing = Drawing(490, 290)
    def box(x, y, w, h, lines):
        drawing.add(Rect(x, y, w, h, rx=5, ry=5, fillColor=colors.HexColor('#eff5fa'), strokeColor=colors.HexColor('#53738e')))
        for index, label in enumerate(lines):
            drawing.add(String(x+w/2, y+h/2+5*(len(lines)-1)-index*12, label, textAnchor='middle', fontName='Helvetica', fontSize=9))
    def arrow(x1, y1, x2, y2):
        drawing.add(Line(x1, y1, x2, y2, strokeColor=colors.HexColor('#53738e')))
        if x1 == x2:
            drawing.add(Polygon([x2,y2,x2-3,y2+6,x2+3,y2+6],fillColor=colors.HexColor('#53738e'),strokeColor=None))
        else:
            drawing.add(Polygon([x2,y2,x2-6,y2-3,x2-6,y2+3],fillColor=colors.HexColor('#53738e'),strokeColor=None))
    box(145,245,200,38,['Terraform + Kind','control plane e worker'])
    arrow(245,245,245,222)
    box(145,185,200,37,['Service HTTP','ConfigMap + Secret'])
    arrow(245,185,245,160)
    box(145,115,200,45,['Aplicação Java 21 Distroless','Deployment: 2 a 4 réplicas','Probes + portas e adaptadores'])
    box(0,115,120,45,['Metrics Server','HPA CPU / memória'])
    arrow(120,137,145,137)
    arrow(185,115,185,83)
    box(100,35,170,48,['PostgreSQL 16 + PVC','OS, estoque, eventos','e notificações pendentes'])
    arrow(345,137,365,137)
    box(365,115,120,45,['Worker de e-mail','SMTP / Mailpit'])
    return drawing


paragraph('Tech Challenge - Fase 2', 'Heading')
paragraph('Oficina mecânica | Software Architecture', 'Heading2')
paragraph('Grupo 111<br/>João Pedro de Souza Sobrinho')
if not args.video_url:
    paragraph('<b>Prévia para conferência.</b> O vídeo ainda precisa ser gravado e publicado. Esta versão não deve ser enviada como entrega final.')
paragraph('Repositório', 'Heading2')
repo = 'https://github.com/joaopedrosobrinho/tech-challenge-oficina'
paragraph(f'<link href="{repo}" color="#245c8a">{repo}</link>')
branch_url = repo + '/tree/tech-challenge-oficina-fase-2'
paragraph(f'Código da fase 2: <link href="{branch_url}" color="#245c8a">branch tech-challenge-oficina-fase-2</link>.')
paragraph('Demonstração', 'Heading2')
if args.video_url:
    paragraph(f'<link href="{escape(args.video_url)}" color="#245c8a">{escape(args.video_url)}</link>')
else:
    paragraph('Link do vídeo: pendente. A demonstração deve apresentar deploy, pipeline, APIs, notificações e escala automática em até 15 minutos.')
paragraph('Solução', 'Heading2')
paragraph('A fase 2 evolui a base da primeira entrega: cadastros de clientes e veículos, catálogo, estoque, orçamento, ciclo da ordem de serviço, acompanhamento público e indicadores. A modelagem DDD e os documentos da primeira fase permanecem disponíveis no repositório.')
paragraph('Monólito com arquitetura hexagonal, domínio independente de JPA e portas para persistência, cadastros, tokens e notificações. A fila operacional prioriza status e antiguidade. A aprovação externa identifica eventos e versões do orçamento para impedir reprocessamento e decisões desatualizadas.')
paragraph('As mudanças de status geram mensagens persistidas na mesma transação da ordem. Um worker envia os avisos por SMTP com novas tentativas. O ambiente local usa Mailpit para visualizar as mensagens sem entregá-las a clientes reais.')
story.append(PageBreak())
paragraph('Arquitetura e deploy', 'Heading')
story.append(diagram())
story.append(Spacer(1, 14))
paragraph('Infraestrutura', 'Heading2')
paragraph('Terraform provisiona o cluster Kind e o PostgreSQL. Os manifestos definem Deployments, Services, ConfigMaps, Secrets, volume persistente, probes, orçamento de interrupções e HPA. O banco tem uma réplica; a alta disponibilidade de banco e a hospedagem cloud não fazem parte deste laboratório.')
paragraph('Fluxo de publicação', 'Heading2')
paragraph('GitHub Actions executa build e testes em PostgreSQL, constrói a imagem Docker, aplica Terraform e publica banco e aplicação. Em seguida, verifica as APIs, gera carga com k6, observa o HPA, coleta relatórios e remove o ambiente descartável.')
paragraph('Verificação e evidências', 'Heading2')
paragraph('O repositório inclui testes de domínio, casos de uso, segurança, persistência, integração externa e notificações. O JaCoCo exige cobertura mínima de 80% por instruções no domínio. Os comandos de teste, deploy e carga estão no README e em docs/FASE-2.md.')


def footer(canvas, document):
    canvas.setFont('Helvetica', 8)
    canvas.setFillColor(colors.HexColor('#536577'))
    canvas.drawString(48, 28, 'Grupo 111 | Tech Challenge Oficina | Fase 2')
    canvas.drawRightString(A4[0]-48, 28, str(document.page))


SimpleDocTemplate(str(destination), pagesize=A4, rightMargin=48, leftMargin=48,
                  topMargin=44, bottomMargin=45, title='Tech Challenge Oficina - Fase 2',
                  author='João Pedro de Souza Sobrinho').build(story, onFirstPage=footer, onLaterPages=footer)
print(destination)
