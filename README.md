### 1. **Configuração Inicial**
- [X] **1.1:** O servidor deve escutar numa porta específica via UDP.
- [X] **1.2:** O cliente deve ser capaz de se conectar ao servidor através de UDP.

### 2. **Comunicação Básica**
- [X] **2.1:** O cliente deve ser capaz de enviar uma mensagem simples para o servidor.
- [X] **2.2:** O servidor deve ser capaz de receber uma mensagem simples do cliente.
- [X] **2.3:** O servidor deve ser capaz de enviar uma resposta para o cliente.
- [X] **2.4:** O cliente deve ser capaz de receber a resposta do servidor.

### 3. **Funcionalidades do Jogo**

- [X] **3.1:** O servidor deve esperar que todos os jogadores se conectem para iniciar o jogo
- [X] **3.2:** O servidor deve enviar uma mensagem para que o cliente altere o estado para receber a escolha de lado dos jogadores
- [X] **3.3:** O servidor deve receber as escolhas dos jogadores e lançar um erro caso o lado que o jogador tentou escolher já tenha sido escolhido
- [X] **3.4:** O servidor deve tratar corretamente o caso em que o jogador usa um valor inválido para escolher o lado (como não inteiros, símbolos, etc.)
- [X] **3.5:** Após todos os jogadores escolherem os seus lados, o servidor deve alterar o estado para receber as jogadas de cada jogador
- [X] **3.6:** O servidor deve tratar corretamente o caso em que o jogador envia um valor de jogada errado (como não inteiros, símbolos, etc.)
- [X] **3.7:** Após todos os jogadores escolherem as suas jogadas, o servidor deve alterar o estado para computar o resultado da partida
- [X] **3.8:** Após computar o resultado, o servidor deve enviar mensagens informando o resultado contextual (vencedor/perdedor) para os jogadores
- [X] **3.9:** Ao finalizar uma partida, o servidor deve alterar o estado para que os jogadores possam decidir se desejam continuar a jogar ou parar
- [X] **3.9.1:** Caso qualquer um dos jogadores decida parar de jogar, o servidor deve ser desligado e os clientes informados que devem finalizar a conexão 

### Classes

- **Player**: classe responsável por armazenar as informações de rede do jogador e informações do jogo relevantes ao contexto do jogador
- **OddEven**: classe responsável por controlar as regras do jogo
- **UDPServer/UDPClient:** classes responsáveis por estabelecer a conexão e manter a comunicação entre cliente e servidor
- **Connection**: classe utilizada apenas no cliente para administrar a conexão com o servidor e receber/enviar mensagens
  - **Receiver**: classe que extende Thread e utiliza uma fila para receber as mensagens e aguardar que o método readMessage seja usado para ler mensagens da fila
  - **Broadcaster**: classe que extende Thread e utiliza uma fila para enviar as mensagens conforme disponibilidade do thread
- **ClientPacket**: classe auxiliar record criada para ser utilizada nos threads do servidor contendo as informações do cliente que deve receber a mensagem
- **State**: classe abstrata para implementação do _State Pattern_ sendo herdada por todas as classes que implementam um estado do jogo