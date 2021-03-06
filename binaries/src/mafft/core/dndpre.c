#include "mltaln.h"

#define TEST 0

static int treeout = 0;

#ifdef enablemultithread
typedef struct _jobtable
{
    int i;  
    int j;  
} Jobtable;

typedef struct _thread_arg
{
	int njob;
	int thread_no;
	float *selfscore;
	double **mtx;
	char **seq;
	Jobtable *jobpospt;
	pthread_mutex_t *mutex;
} thread_arg_t;

void *athread( void *arg )
{
	thread_arg_t *targ = (thread_arg_t *)arg;
	int njob = targ->njob;
	int thread_no = targ->thread_no;
	float *selfscore = targ->selfscore;
	double **mtx = targ->mtx;
	char **seq = targ->seq;
	Jobtable *jobpospt = targ->jobpospt;

	int i, j;
	float ssi, ssj, bunbo;
	
	while( 1 )
	{
		pthread_mutex_lock( targ->mutex );
		j = jobpospt->j;
		i = jobpospt->i;
		j++;
		if( j == njob )
		{
			fprintf( stderr, "%4d/%4d (thread %4d), dndpre\r", i+1, njob, thread_no );
			i++;
			j = i + 1;
			if( i == njob-1 )
			{
				pthread_mutex_unlock( targ->mutex );
				return( NULL );
			}
		}
		jobpospt->j = j;
		jobpospt->i = i;
		pthread_mutex_unlock( targ->mutex );

		ssi = selfscore[i];
		ssj = selfscore[j];

		bunbo = MIN( ssi, ssj );
		if( bunbo == 0.0 )
			mtx[i][j] = 1.0;
		else
			mtx[i][j] = 1.0 - (double)naivepairscore11( seq[i], seq[j], penalty ) / bunbo;
	}
}

#endif

void arguments( int argc, char *argv[] )
{
    int c;

	nthread = 1;
	alg = 'X';
	fmodel = 0;
	treeout = 0;
	scoremtx = 1;
	nblosum = 62;
	dorp = NOTSPECIFIED;
	inputfile = NULL;
	ppenalty = NOTSPECIFIED; //?
	ppenalty_ex = NOTSPECIFIED; //?
	poffset = NOTSPECIFIED; //?
	kimuraR = NOTSPECIFIED;
	pamN = NOTSPECIFIED;

    while( --argc > 0 && (*++argv)[0] == '-' )
	{
        while ( (c = *++argv[0]) )
		{
            switch( c )
            {
				case 't':
					treeout = '1';
					break;
				case 'D':
					dorp = 'd';
					break;
				case 'a':
					fmodel = 1;
					break;
				case 'P':
					dorp = 'p';
					break;
				case 'i':
					inputfile = *++argv;
					fprintf( stderr, "inputfile = %s\n", inputfile );
					--argc;
					goto nextoption;
				case 'C':
					nthread = atoi( *++argv );
					fprintf( stderr, "nthread = %d\n", nthread );
					--argc; 
					goto nextoption;
            }
		}
		nextoption:
			;
	}
    if( argc != 0 ) 
	{
		fprintf( stderr, "options: Check source file !\n" );
		exit( 1 );
	}
}

int main( int argc, char **argv )
{
	int i, j;
	char **seq;
	static char **name;
	static int nlen[M];
	float *selfscore;
	double **mtx;
	FILE *fp;
	FILE *infp;
	float ssi, ssj, bunbo;


	arguments( argc, argv );
#ifndef enablemultithread
	nthread = 0;
#endif

	if( inputfile )
	{
		infp = fopen( inputfile, "r" );
		if( !infp )
		{
			fprintf( stderr, "Cannot open %s\n", inputfile );
			exit( 1 );
		}
	}
	else
		infp = stdin;

#if 0
	PreRead( stdin, &njob, &nlenmax );
#else
	getnumlen( infp );
#endif
	rewind( infp );

	seq = AllocateCharMtx( njob, nlenmax+1 );
	name = AllocateCharMtx( njob, B+1 );
	mtx = AllocateDoubleMtx( njob, njob );
	selfscore = AllocateFloatVec( njob );

#if 0
	FRead( stdin, name, nlen, seq );
#else
	readData_pointer( infp, name, nlen, seq );
#endif
	fclose( infp );

	constants( njob, seq );

#if 0
	for( i=0; i<njob-1; i++ ) 
	{
		fprintf( stderr, "%4d/%4d\r", i+1, njob );
		for( j=i+1; j<njob; j++ ) 
			mtx[i][j] = (double)substitution_hosei( seq[i], seq[j] );
//			fprintf( stderr, "i=%d,j=%d, l=%d &&&  %f\n", i, j, nlen[0], mtx[i][j] );
	}
#else // 061003
	for( i=0; i<njob; i++ )
	{
		selfscore[i] = (float)naivepairscore11( seq[i], seq[i], penalty );

	}
#ifdef enablemultithread
	if( nthread > 0 )
	{
		thread_arg_t *targ;
		Jobtable jobpos;
		pthread_t *handle;
		pthread_mutex_t mutex;

		jobpos.i = 0;
		jobpos.j = 0;

		targ = calloc( nthread, sizeof( thread_arg_t ) );
		handle = calloc( nthread, sizeof( pthread_t ) );
		pthread_mutex_init( &mutex, NULL );

		for( i=0; i<nthread; i++ )
		{
			targ[i].thread_no = i;
			targ[i].njob = njob;
			targ[i].selfscore = selfscore;
			targ[i].mtx = mtx;
			targ[i].seq = seq;
			targ[i].jobpospt = &jobpos;
			targ[i].mutex = &mutex;

			pthread_create( handle+i, NULL, athread, (void *)(targ+i) );
		}

		for( i=0; i<nthread; i++ )
		{
			pthread_join( handle[i], NULL );
		}
		pthread_mutex_destroy( &mutex );
	}
	else
#endif
	{
		for( i=0; i<njob-1; i++ )
		{
			ssi = selfscore[i];
			fprintf( stderr, "%4d/%4d\r", i+1, njob );
			for( j=i+1; j<njob; j++ )
			{
				ssj = selfscore[j];
				bunbo = MIN( ssi, ssj );
				if( bunbo == 0.0 )
					mtx[i][j] = 1.0;
				else
					mtx[i][j] = 1.0 - (double)naivepairscore11( seq[i], seq[j], penalty ) / bunbo;
//					mtx[i][j] = 1.0 - (double)naivepairscore11( seq[i], seq[j], penalty ) / MIN( selfscore[i], selfscore[j] );
//				fprintf( stderr, "i=%d,j=%d, l=%d### %f, score = %d\n", i, j, nlen[0], mtx[i][j], naivepairscore11( seq[i], seq[j], penalty )  );
			}
		}
	}
#endif
	
#if TEST
	for( i=0; i<njob-1; i++ ) for( j=i+1; j<njob; j++ ) 
		fprintf( stdout, "i=%d, j=%d, mtx[][] = %f\n", i, j, mtx[i][j] );
#endif

	fp = fopen( "hat2", "w" );
	WriteHat2_pointer( fp, njob, name, mtx );
	fclose( fp );
#if 0
	if( treeout )
	{
		int ***topol;
		double **len;

		topol = AllocateIntCub( njob, 2, njob );
		len = AllocateDoubleMtx( njob, njob );
		veryfastsupg_double_outtree( njob, mtx, topol, len );
	}
#endif
	SHOWVERSION;
	exit( 0 );
/*
	res = system( ALNDIR "/spgsdl < hat2"  );
	if( res ) exit( 1 );
	else exit( 0 );
*/
}
