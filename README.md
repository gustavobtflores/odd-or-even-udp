### 1. **Configuração Inicial**
- [X] **1.1:** O servidor deve escutar numa porta específica via UDP.
- [X] **1.2:** O cliente deve ser capaz de se conectar ao servidor através de UDP.

### 2. **Comunicação Básica**
- [X] **2.1:** O cliente deve ser capaz de enviar uma mensagem simples para o servidor.
- [X] **2.2:** O servidor deve ser capaz de receber uma mensagem simples do cliente.
- [X] **2.3:** O servidor deve ser capaz de enviar uma resposta para o cliente.
- [X] **2.4:** O cliente deve ser capaz de receber a resposta do servidor.

### 3. **Funcionalidades do Jogo**

- [ ] **3.1:** O servidor deve esperar que todos os jogadores se conectem e enviem uma mensagem de pronto para iniciar a partida
- [ ] **3.2:** O servidor deve controlar os turnos dos jogadores, enviando uma mensagem para o jogador que estiver na vez


### Classes

- Player: classe responsável por armazenar as informações de rede do jogador e eventualmente alguma informação sobre o jogo
- OddEven: classe responsável por controlar as regras do jogo (pode receber uma lista de jogadores para controlar os turnos)
- UDPServer/UDPClient - classes que controlam a conexão na rede