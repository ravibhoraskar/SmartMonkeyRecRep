
title = manual
latex  = pdflatex 
papers = $(title).tex

all: pdf

pdf : $(papers)
	$(latex) $(papers)
	bibtex $(title)
	$(latex) $(papers)
	$(latex) $(papers)

clean:
	rm -rf $(title).bbl $(title).aux $(title).pdf $(title).blg $(title).log
