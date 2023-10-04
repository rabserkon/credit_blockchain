# credit_blockchain
## Description
credit_blockchain is a small blockchain network designed for crediting participants using Proof of Stake (PoS) consensus. The main goal of the project is to create a decentralized network that allows participants to store cryptocurrency, use smart contracts for collateral and assess credit risk based on previous refunds. This project is designed to improve the availability of credit and provide a more transparent and secure process.
## Structure
### Block
Block contains information about the block's hash, the previous hash, the creator, timestamp, transaction lists, and other related attributes. This class also includes methods for calculating hashes, adding transactions, serialization and deserialization, as well as signature and balance verification.
### Transaction
Transaction class represents transactions in the blockchain. It contains sender, recipient, amount, and a digital signature. It provides methods for signature generation and verification, as well as hash calculation. The StringUtil class offers utility functions for cryptographic operations.
### Wallet
Wallet class manages a user's blockchain wallet, including generating key pairs, creating and signing transactions, calculating balances, and persisting wallet data to files. It holds a user's private and public keys, and supports creating transactions.
### P2P Node
The P2P Node class represents a peer-to-peer (P2P) node in a blockchain network. It manages communication with other peers, handles incoming messages, and maintains the blockchain and a list of validators. The node can receive blocks from other peers, register validators, and add new blocks to the blockchain. It listens on a specified port for incoming connections and processes messages from connected peers.
