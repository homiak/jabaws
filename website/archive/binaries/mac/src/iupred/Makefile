CFLAGS = -O3 -funroll-loops -Winline -I./
LDLIBS = -lm 

EXE = iupred

RM = rm -f
CP = cp

GPP = cc
LD = $(GPP) $(CFLAGS)
CPP = $(GPP) -c $(CFLAGS) 
                   

all: iupred

CPPSRC = $(sort $(wildcard *.c))
CPPOBJ	= $(subst .c,.o,$(CPPSRC))

$(CPPOBJ): %.o: %.c
	$(CPP) $< -o $@

iupred: $(CPPOBJ)
	$(LD) -o $(EXE) $(CPPOBJ) $(LDLIBS)
	strip $(EXE)
clean:
	$(RM) $(CPPOBJ)
	$(RM) $(EXE)
