from subprocess import Popen, PIPE, STDOUT
import sys
import random

cmd_client = 'java -jar build/libs/generalClientServer-1.0.jar client localhost 13370'.split(
    ' ')
cmd_server = 'java -jar build/libs/generalClientServer-1.0.jar server localhost 13370'.split(
    ' ')


def genFibArray(toN):
    lst = [0, 1]
    for i in range(2, toN+1):
        lst.append(lst[-2]+lst[-1])
    return lst


def sequent_test(n, t, maxN=100000):
    server = Popen(cmd_server, stdout=PIPE, stdin=PIPE, stderr=STDOUT)
    ans = sequent_test_impl(n, t, maxN)
    server.terminate()
    return ans


def sequent_test_impl(n, t, maxN=100000):
    client_pool = []
    for i in range(n):
        client = Popen(cmd_client, stdout=PIPE, stdin=PIPE, stderr=STDOUT)
        client_pool.append(client)

    good = True
    fibArray = genFibArray(maxN)
    for i in range(t):
        c = client_pool[random.randint(0, len(client_pool)-1)]
        x = random.randint(0, maxN)
        c.stdin.write(bytes(str(x)+'\n', 'UTF-8'))
        c.stdin.flush()
        ans = c.stdout.readline()

        try:
            ans = int(ans.rstrip())
        except RuntimeError:
            good = False
            break

        if ans != fibArray[x]:
            good = False
            break

    for client in client_pool:
        client.terminate()

    return good
