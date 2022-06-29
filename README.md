# Programming-Concurrent-and-Distributed-ISCTE-Project
Communication between nodes within a network

This Project has the objective of using a single node(client with a array of 1 000 000 bytes) connected to a server and the next node(client without data) connected will request the information from the nodes available in the network, allowing the more nodes with information there are available, the faster the download will be.

Example:
1 node with data --> 1 node without data


![code 1](https://user-images.githubusercontent.com/89052833/176490214-c5e80b7d-cfd3-4e48-a4be-a5425b4fb50b.png)


2 node with data --> 1 node without data

![code2](https://user-images.githubusercontent.com/89052833/176490575-bc249012-4db1-454f-9a7e-473372c3037c.png)

After each node has its own data we can corrupt the bytes by inputting the command "ERROR" + array position(int) in order for the error to be detected by the error detection threads and in turn to correct this error by sending a request to the other nodes available on the network for the respective information in the position where the error was detected. if the information is the same in the other nodes, then we correct the error.


![code 3](https://user-images.githubusercontent.com/89052833/176493202-d25ff091-f9fe-40f5-84bd-4e0ab2e72e73.png)


we can also check the data of a custom portion of the array of each node by using its own GUI that connects to the server and the node port that we want to check.

![code 4](https://user-images.githubusercontent.com/89052833/176493977-1cb2054f-2fc4-4b6c-b6a8-790d5b8ded58.png)
