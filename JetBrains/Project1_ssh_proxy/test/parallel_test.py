from subprocess import Popen, PIPE, STDOUT
import functools
from multiprocessing import Pool
from sequent_test import sequent_test_impl, cmd_server

def parallel_test(n, seqArgs):
    pool = Pool(processes=n)
    server = Popen(cmd_server, stdout=PIPE, stdin=PIPE, stderr=STDOUT)
    seqtest = functools.partial(sequent_test_impl, *seqArgs)
    res = pool.map(seqtest, [0] * n)
    server.terminate()
    return not (False in res)
