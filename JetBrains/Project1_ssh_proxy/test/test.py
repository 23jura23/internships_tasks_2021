import time
from sequent_test import sequent_test
from parallel_test import parallel_test

def test():
    ok = True

    start = time.perf_counter()
    print('sequent_test(1, 10)', end='')
    if not sequent_test(1, 10):
        ok = False
        print(' failed', end='')
    end = time.perf_counter()
    print('')
    print('Time elapsed: {:.3f}s\n'.format(end-start))

    start = time.perf_counter()
    print('sequent_test(10, 1000)', end='')
    if not sequent_test(10, 1000):
        ok = False
        print(' failed', end='')
    end = time.perf_counter()
    print('')
    print('Time elapsed: {:.3f}s\n'.format(end-start))

    start = time.perf_counter()
    print('sequent_test(100, 100)', end='')
    if not sequent_test(100, 100):
        ok = False
        print(' failed', end='')
    end = time.perf_counter()
    print('')
    print('Time elapsed: {:.3f}s\n'.format(end-start))
    
    start = time.perf_counter()
    print('sequent_test(1000, 10)', end='')
    if not sequent_test(100, 100):
        ok = False
        print(' failed', end='')
    end = time.perf_counter()
    print('')
    print('Time elapsed: {:.3f}s\n'.format(end-start))

    start = time.perf_counter()
    print('parallel_test(1, (1, 10))', end='')
    if not parallel_test(1, (1, 10)):
        ok = False
        print(' failed', end='')
    end = time.perf_counter()
    print('')
    print('Time elapsed: {:.3f}s\n'.format(end-start))

    start = time.perf_counter()
    print('parallel_test(10, (5, 1000))', end='')
    if not parallel_test(10, (5, 1000)):
        ok = False
        print(' failed', end='')
    end = time.perf_counter()
    print('')
    print('Time elapsed: {:.3f}s\n'.format(end-start))
    
    start = time.perf_counter()
    print('parallel_test(10, (2, 5000))', end='')
    if not parallel_test(10, (2, 5000)):
        ok = False
        print(' failed', end='')
    end = time.perf_counter()
    print('')
    print('Time elapsed: {:.3f}s\n'.format(end-start))

    if ok:
        print('Tests passed')
    else:
        print('Tests failed')

if __name__ == '__main__':
    test()
