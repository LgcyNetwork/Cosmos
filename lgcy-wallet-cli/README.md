Welcome to use the Wallet-CLI.  

## Get started

### Edit config.conf in src/main/resources

```
net {
  type = mainnet
  # type = testnet
}

fullnode = {
  ip.list = [
    "fullnode ip : port"
  ]
}

soliditynode = {
  ip.list = [
    "solidity ip : port"
  ]
} // NOTE: solidity node is optional

blockNumberStartToScan = 22690588 // NOTE: this field is optional
```

### Run a web wallet

- connect to fullNode and solidityNode

    Run both fullNode and solidity node in either your local PC or remote server.

    NOTE: These nodes would consume a lot of memory and CPU. Please be aware if you do not use wallet, just kill them.
- compile and run web wallet

    ```console
    $ cd wallet-cli
    $ ./gradlew build
    $ cd build/libs
    $ java -jar wallet-cli.jar
    ```

### Connect to lgcy

Wallet-cli connect to lgcy via gRPC protocol, which can be deployed locally or remotely. Check **Run a web Wallet** section.
We can configure lgcy node IP and port in ``src/main/resources/config.conf``, so that wallet-cli server can successfully talk to lgcy nodes.

## Wallet-cli supported command list

Following is a list of Lgcy Wallet-cli commands:
For more information on a specific command, just type the command on terminal when you start your Wallet.

| [AddTransactionSign](#How-to-use-the-multi-signature-feature-of-wallet-cli) | [ApproveProposal](#Approvecancel-the-proposal)  | [AssetIssue](#How-to-issue-TRC10-tokens) |
| :---------:|:---------:|:--------: |
| [BackupShieldedTRC20Wallet](#How-to-transfer-shielded-TRC20-token) | [BackupWallet](#Wallet-related-commands)| [BackupWallet2Base64](#Wallet-related-commands) |
| [BroadcastTransaction](#Some-others) | [ChangePassword](#Wallet-related-commands)| [CreateProposal](#How-to-initiate-a-proposal) 
| [DeleteProposal](#Cancel-the-created-proposal) | [DeployContract](#How-to-use-smart-contract) | [ExchangeCreate](#How-to-trade-on-the-exchange) |
| [ExchangeInject](#How-to-trade-on-the-exchange) | [ExchangeTransaction](#How-to-trade-on-the-exchange) | [ExchangeWithdraw](#How-to-trade-on-the-exchange) |
| [FreezeBalance](#How-to-delegate-resourcee) | [GenerateAddress](#Account-related-commands) | [GenerateShieldedTRC20Address](#How-to-transfer-shielded-TRC20-token)|
| [GetAccount](#Account-related-commands) |[GetAccountNet](#Account-related-commands) | [GetAccountResource](#Account-related-commands) | 
| [GetAddress](#Account-related-commands) | [GetAkFromAsk](#How-to-transfer-shielded-TRC20-token) |[GetAssetIssueByAccount](#How-to-issue-TRC10-tokens) | 
| [GetAssetIssueById](#How-to-issue-TRC10-tokens) | [GetAssetIssueByName](#How-to-issue-TRC10-tokens) |[GetAssetIssueListByName](#How-to-issue-TRC10-tokens) | 
| [GetBalance](#Account-related-commands) | [GetBlock](#How-to-get-block-information) |[GetBlockById](#How-to-get-block-information) | 
| [GetBlockByLatestNum](#How-to-get-block-information) | [GetBlockByLimitNext](#How-to-get-block-information) | [GetBrokerage](#Brokerage) | 
| [GetContract](#How-to-use-smart-contracts) | [GetDelegatedResource](#How-to-delegate-resource) |[GetDelegatedResourceAccountIndex](#How-to-delegate-resource) | 
| [GetDiversifier](#How-to-transfer-shielded-TRC20-token)| [GetExpandedSpendingKey](#How-to-transfer-shielded-TRC20-token)| [GetIncomingViewingKey](#How-to-transfer-shielded-TRC20-token)  | 
| [GetMarketOrderByAccount](#How-to-use-lgcy-to-sell-asset)| [GetMarketOrderById](#How-to-use-lgcy-to-sell-asset)| [GetMarketOrderListByPair](#How-to-use-lgcy-to-sell-asset)  | 
| [GetMarketPairList](#How-to-use-lgcy-to-sell-asset)| [GetMarketPriceByPair](#How-to-use-lgcy-to-sell-asset)| [GetNextMaintenanceTime](#Some-others) | 
| [GetNkFromNsk](#How-to-transfer-shielded-TRC20-token) | [GetProposal](#Get-proposal-information) | [GetShieldedPaymentAddress](#How-to-transfer-shielded-TRC20-token)| 
| [GetSpendingKey](#How-to-transfer-shielded-TRC20-token) | [GetReward](#Brokerage) |  [GetTransactionApprovedList](#How-to-use-the-multi-signature-feature-of-wallet-cli) |
| [GetTransactionById](#How-to-get-transaction-information) | [GetTransactionCountByBlockNum](#How-to-get-transaction-information) | [GetTransactionInfoByBlockNum](#How-to-get-transaction-information) | 
| [GetTransactionInfoById](#How-to-get-transaction-information) | [GetTransactionSignWeight](#How-to-use-the-multi-signature-feature-of-wallet-cli) | [ImportShieldedTRC20Wallet](#How-to-transfer-shielded-TRC20-token) | 
| [ImportWallet](#Wallet-related-commands) | [ImportWalletByBase64](#Wallet-related-commands) | [ListAssetIssue](#Get-Token10) | 
| [ListExchanges](#How-to-trade-on-the-exchange) | [ListExchangesPaginated](#How-to-trade-on-the-exchange) | [ListNodes](#Some-others) | 
| [ListShieldedTRC20Address](#How-to-transfer-shielded-TRC20-token) | [ListShieldedTRC20Note](#How-to-transfer-shielded-TRC20-token) | [ListProposals](#How-to-initiate-a-proposal) | 
| [ListProposalsPaginated](#How-to-initiate-a-proposal) | [ListWitnesses](#Some-others) | [LoadShieldedTRC20Wallet](#How-to-transfer-shielded-TRC20-token) | 
| [Login](#Command-line-operation-flow-example) | [MarketCancelOrder](#How-to-use-lgcy-to-sell-asset) | [MarketSellAsset](#How-to-use-lgcy-to-sell-asset)| 
| [ParticipateAssetIssue](#How-to-issue-TRC10-tokens) | [RegisterWallet](#Wallet-related-commands) | [ResetShieldedTRC20Note](#How-to-transfer-shielded-TRC20-token) | 
| [ScanShieldedTRC20NoteByIvk](#How-to-transfer-shielded-TRC20-token) |  [ScanShieldedTRC20NoteByOvk](#How-to-transfer-shielded-TRC20-token) |[SendCoin](#How-to-use-the-multi-signature-feature-of-wallet-cli) | 
| [SendShieldedTRC20Coin](#How-to-transfer-shielded-TRC20-token) | [SendShieldedTRC20CoinWithoutAsk](#How-to-transfer-shielded-TRC20-token) | [SetShieldedTRC20ContractAddress](#How-to-transfer-shielded-TRC20-token) | 
| [ShowShieldedTRC20AddressInfo](#How-to-transfer-shielded-TRC20-token) | [TransferAsset](#How-to-issue-TRC10-tokens) | [TriggerContract](#How-to-use-smart-contracts) |
| [UnfreezeAsset](#How-to-issue-TRC10-tokens) | [UnfreezeBalance](#How-to-delegate-resource) |[UpdateAsset](#How-to-issue-TRC10-tokens) | 
| [UpdateBrokerage](#Brokerage) | [UpdateKandyLimit](#How-to-use-smart-contracts) |[UpdateSetting](#How-to-use-smart-contracts) | 
| [UpdateAccountPermission](#How-to-use-the-multi-signature-feature-of-wallet-cli) | [VoteWitness](#How-to-vote) |

Type any one of the listed commands, to display how-to tips.

## How to freeze/unfreeze balance

After the funds are frozen, the corresponding number of shares and bandwidth will be obtained.
Shares can be used for voting and bandwidth can be used for trading.
The rules for the use and calculation of share and bandwidth are described later in this article.

**Freeze operation is as follows:**

```console
> freezeBalance [OwnerAddress] frozen_balance frozen_duration [ResourceCode:0 BANDWIDTH, 1 USDL_POWER] [receiverAddress]
```

OwnerAddress
> The address of the account that initiated the transaction, optional, default is the address of the login account.

frozen_balance
> The amount of frozen funds, the unit is Sun.
> The minimum value is **1000000 Sun(1USDL)**.

frozen_duration
> Freeze time, this value is currently only allowed for **3 days**.

For example:

```console
> freezeBalance 100000000 3 1 address
```

After the freeze operation, frozen funds will be transferred from Account Balance to Frozen,
You can view frozen funds from your account information.
After being unfrozen, it is transferred back to Balance by Frozen, and the frozen funds cannot be used for trading.

When more share or bandwidth is needed temporarily, additional funds may be frozen to obtain additional share and bandwidth.
The unfrozen time is postponed until 3 days after the last freeze operation

After the freezing time expires, funds can be unfroze.

**Unfreeze operation is as follows:**

```console
> unfreezeBalance [OwnerAddress] ResourceCode(0 BANDWIDTH, 1 CPU) [receiverAddress]
```

## How to vote

Voting requires share. Share can be obtained by freezing funds.

- The share calculation method is: **1** unit of share can be obtained for every **1USDL** frozen.
- After unfreezing, previous vote will expire. You can avoid the invalidation of the vote by re-freezing and voting.

**NOTE** The Lgcy Network only records the status of your last vote, which means that each of your votes will overwrite all previous voting results.

For example:

```console
> freezeBalance 100000000 3 1 address  # Freeze 10USDL and acquire 10 units of shares

> votewitness 123455 witness1 4 witness2 6  # Cast 4 votes for witness1 and 6 votes for witness2 at the same time

> votewitness 123455 witness1 10  # Voted 10 votes for witness1
```

The final result of the above command was 10 votes for witness1 and 0 vote for witness2.

## Brokerage

After voting for the witness, you will receive the rewards. The witness has the right to decide the ratio of brokerage. The default ratio is 20%, and the witness can adjust it.

By default, if a witness is rewarded, he will receive 20% of the whole rewards, and 80% of the rewards will be distributed to his voters.

### GetBrokerage

View the ratio of brokerage of the witness.

    > getbrokerage OwnerAddress

OwnerAddress
> The address of the witness's account, it is a base58check type address.

### GetReward

Query unclaimed reward.

    > getreward OwnerAddress

OwnerAddress
> The address of the voter's account, it is a base58check type address.

### UpdateBrokerage

Update the ratio of brokerage, this command is usually used by a witness account.

    > updateBrokerage OwnerAddress brokerage

OwnerAddress
> The address of the witness's account, it is a base58check type address.

brokerage
> The ratio of brokerage you want to update to, the limit of it: 0-100.

For example:

```console
> getbrokerage TZ7U1WVBRLZ2umjizxqz3XfearEHhXKX7h  

> getreward  TNfu3u8jo1LDWerHGbzs2Pv88Biqd85wEY

> updateBrokerage TZ7U1WVBRLZ2umjizxqz3XfearEHhXKX7h 30
```

## How to calculate bandwidth

The bandwidth calculation rule is:

    constant * FrozenFunds * days

Assuming freeze 1USDL（1_000_000 Sun), 3 days, bandwidth obtained = 1 * 1_000_000 * 3 = 3_000_000.

All contracts consume bandwidth, including transferring, transferring of assets, voting, freezing, etc.
Querying does not consume bandwidth. Each contract needs to consume **100_000 bandwidth**.

If a contract exceeds a certain time (**10s**), this operation does not consume bandwidth.

When the unfreezing operation occurs, the bandwidth is not cleared.
The next time the freeze is performed, the newly added bandwidth is accumulated.

## How to withdraw balance

After each block is produced, the block award is sent to the account's allowance,
and a withdraw operation is allowed every **24 hours** from allowance to balance.
The funds in allowance cannot be locked or traded.

## How to create witness

Applying to become a witness account needs to consume **100_000USDL**.
This part of the funds will be burned directly.

## How to create account

It is not allowed to create accounts directly. You can only create accounts by transferring funds to non-existing accounts.
Transferring to a non-existent account has minimum restriction amount of **1USDL**.

## Command line operation flow example

```console
$ cd wallet-cli
$ ./gradlew build
$ ./gradlew run
> RegisterWallet 123456      (password = 123456)
> login 123456
> getAddress
address = TRfwwLDpr4excH4V4QzghLEsdYwkapTxnm'  # backup it!
> BackupWallet 123456
priKey = 075725cf903fc1f6d6267b8076fc2c6adece0cfd18626c33427d9b2504ea3cef'  # backup it!!! (BackupWallet2Base64 option)
> getbalance
Balance = 0
> AssetIssue TestUSDL USDL 75000000000000000 1 1 2 "2019-10-02 15:10:00" "2020-07-11" "just for test121212" www.test.com 100 100000 10000 10 10000 1
> getaccount TRfwwLDpr4excH4V4QzghLEsdYwkapTxnm
(Print balance: 9999900000
"assetV2": [
    {
        "key": "1000001",
        "value": 74999999999980000
    }
],)
  # (cost usdl 1000 usdl for assetIssue)
  # (You can query the usdl balance and other asset balances for any account )
> TransferAsset TWzrEZYtwzkAxXJ8PatVrGuoSNsexejRiM 1000001 10000
```

## How to issue a TRC10 token

Each account can only issue **ONE** TRC10 token.

### Issue TRC10 tokens

    > AssetIssue [OwnerAddress] AssetName AbbrName TotalSupply USDLNum AssetNum Precision StartDate EndDate Description Url FreeNetLimitPerAccount PublicFreeNetLimit FrozenAmount0 FrozenDays0 [...] FrozenAmountN FrozenDaysN

OwnerAddress (optional)
> The address of the account which initiated the transaction. 
> Default: the address of the login account.

AssetName
> The name of the issued TRC10 token

AbbrName
> The abbreviation of TRC10 token

TotalSupply
> ​TotalSupply = Account Balance of Issuer + All Frozen Token Amount
> TotalSupply: Total Issuing Amount
> Account Balance Of Issuer: At the time of issuance
> All Frozen Token Amount: Before asset transfer and the issuance

USDLNum, AssetNum
>  These two parameters determine the exchange rate when the token is issued.
> Exchange Rate = USDLNum / AssetNum
> AssetNum: Unit in base unit of the issued token
> USDLNum: Unit in SUN (0.000001 USDL)

Precision
> Precision to how many decimal places  

FreeNetLimitPerAccount
> The maximum amount of bandwidth each account is allowed to use. Token issuers can freeze USDL to obtain bandwidth (TransferAssetContract only)

PublicFreeNetLimit
> The maximum total amount of bandwidth which is allowed to use for all accounts. Token issuers can freeze USDL to obtain bandwidth (TransferAssetContract only)

StartDate, EndDate
> The start and end date of token issuance. Within this period time, other users can participate in token issuance.

FrozenAmount0 FrozenDays0
> Amount and days of token freeze. 
> FrozenAmount0: Must be bigger than 0
> FrozenDays0: Must between 1 and 3653.

Example:

```console
> AssetIssue TestUSDL USDL 75000000000000000 1 1 2 "2019-10-02 15:10:00" "2020-07-11" "just for test121212" www.test.com 100 100000 10000 10 10000 1
> GetAssetIssueByAccount TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ  # View published information
{
    "assetIssue": [
        {
            "owner_address": "TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
            "name": "TestUSDL",
            "abbr": "USDL",
            "total_supply": 75000000000000000,
            "frozen_supply": [
                {
                    "frozen_amount": 10000,
                    "frozen_days": 1
                },
                {
                    "frozen_amount": 10000,
                    "frozen_days": 10
                }
            ],
            "usdl_num": 1,
            "precision": 2,
            "num": 1,
            "start_time": 1570000200000,
            "end_time": 1594396800000,
            "description": "just for test121212",
            "url": "www.test.com",
            "free_asset_net_limit": 100,
            "public_free_asset_net_limit": 100000,
            "id": "1000001"
        }
    ]
}
```

### Update parameters of TRC10 token

    > UpdateAsset [OwnerAddress] newLimit newPublicLimit description url

Specific meaning of the parameters is the same as that of AssetIssue.

Example:

```console
> UpdateAsset 1000 1000000 "change description" www.changetest.com
> GetAssetIssueByAccount TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ  # View the modified information
{
    "assetIssue": [
        {
            "owner_address": "TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
            "name": "TestUSDL",
            "abbr": "USDL",
            "total_supply": 75000000000000000,
            "frozen_supply": [
                {
                    "frozen_amount": 10000,
                    "frozen_days": 1
                },
                {
                    "frozen_amount": 10000,
                    "frozen_days": 10
                }
            ],
            "usdl_num": 1,
            "precision": 2,
            "num": 1,
            "start_time": 1570000200000,
            "end_time": 1594396800000,
            "description": "change description",
            "url": "www.changetest.com",
            "free_asset_net_limit": 1000,
            "public_free_asset_net_limit": 1000000,
            "id": "1000001"
        }
    ]
}
```

### TRC10 token transfer

    > TransferAsset [OwnerAddress] ToAddress AssertID Amount

OwnerAddress (optional)
> The address of the account which initiated the transaction. 
> Default: the address of the login account.

ToAddress
> Address of the target account

AssertName
> TRC10 token ID
> Example: 1000001

Amount
> The number of TRC10 token to transfer

Example:

```console
> TransferAsset TN3zfjYUmMFK3ZsHSsrdJoNRtGkQmZLBLz 1000001 1000
> getaccount TN3zfjYUmMFK3ZsHSsrdJoNRtGkQmZLBLz  # View target account information after the transfer
address: TN3zfjYUmMFK3ZsHSsrdJoNRtGkQmZLBLz
    assetV2
    {
    id: 1000001
    balance: 1000
    latest_asset_operation_timeV2: null
    free_asset_net_usageV2: 0
    }
```

### Participating in the issue of TRC10 token

    > ParticipateAssetIssue [OwnerAddress] ToAddress AssetID Amount

OwnerAddress (optional)
> The address of the account which initiated the transaction. 
> Default: the address of the login account.

ToAddress
> Account address of TRC10 issuers

AssertName
> TRC10 token ID
> Example: 1000001

Amount
> The number of TRC10 token to transfers

The participation process must happen during the release of TRC10, otherwise an error may occur.

Example:

```console
> ParticipateAssetIssue TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ 1000001 1000
> getaccount TJCnKsPa7y5okkXvQAidZBzqx3QyQ6sxMW  # View remaining balance
address: TJCnKsPa7y5okkXvQAidZBzqx3QyQ6sxMW
assetV2
    {
    id: 1000001
    balance: 1000
    latest_asset_operation_timeV2: null
    free_asset_net_usageV2: 0
    }
```

### Unfreeze TRC10 token

To unfreeze all TRC10 token which are supposed to be unfrozen after the freezing period.

    > unfreezeasset [OwnerAddress]

## How to obtain TRC10 token information

ListAssetIssue
> Obtain all of the published TRC10 token information

GetAssetIssueByAccount
> Obtain TRC10 token information based on issuing address

GetAssetIssueById
> Obtain TRC10 token Information based on ID

GetAssetIssueByName
> Obtain TRC10 token Information based on names

GetAssetIssueListByName
> Obtain a list of TRC10 token information based on names

## How to operate with proposal

Any proposal-related operations, except for viewing operations, must be performed by committee members.

### Initiate a proposal

    > createProposal [OwnerAddress] id0 value0 ... idN valueN

OwnerAddress (optional)
> The address of the account which initiated the transaction. 
> Default: the address of the login account.

id0
> The serial number of the parameter. Every parameter of LGCY network has a serial number. Please refer to "http://lgcyscan.org/#/sr/committee" 

Value0
> The modified value

In the example, modification No.4 (modifying token issuance fee) costs 1000USDL as follows:

```console
> createProposal 4 1000
> listproposals  # View initiated proposal
{
    "proposals": [
        {
            "proposal_id": 1,
            "proposer_address": "TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
            "parameters": [
                {
                    "key": 4,
                    "value": 1000
                }
            ],
            "expiration_time": 1567498800000,
            "create_time": 1567498308000
        }
    ]
}
```

The corresponding id is 1.

### Approve / Disapprove a proposal

    > approveProposal [OwnerAddress] id is_or_not_add_approval

OwnerAddress (optional)
> The address of the account which initiated the transaction. 
> Default: the address of the login account.

id
> ID of the initiated proposal
> Example: 1

is_or_not_add_approval
> true for approve; false for disapprove

Example:

```console
> ApproveProposal 1 true  # in favor of the offer
> ApproveProposal 1 false  # Cancel the approved proposal
```

### Delete an existed proposal

    > deleteProposal [OwnerAddress] proposalId

proposalId
> ID of the initiated proposal
> Example: 1

The proposal must be canceled by the supernode that initiated the proposal.

Example：

    > DeleteProposal 1

### Obtain proposal information

ListProposals
> Obtain a list of initiated proposals

ListProposalsPaginated
> Use the paging mode to obtain the initiated proposal

GetProposal
> Obtain proposal information based on the proposal ID

### Create a trading pair

    > exchangeCreate [OwnerAddress] first_token_id first_token_balance second_token_id second_token_balance

OwnerAddress (optional)
> The address of the account which initiated the transaction.
> Default: the address of the login account.

First_token_id, first_token_balance
> ID and amount of the first token

second_token_id, second_token_balance
> ID and amount of the second token
>
> The ID is the ID of the issued TRC10 token. 
> If it is USDL, the ID is "_". 
> The amount must be greater than 0, and less than 1,000,000,000,000,000.

Example:

    > exchangeCreate 1000001 10000 _ 10000
    # Create trading pairs with the IDs of 1000001 and USDL, with amount 10000 for both.

### Capital injection

    > exchangeInject [OwnerAddress] exchange_id token_id quant

OwnerAddress (optional)
> The address of the account which initiated the transaction.
> Default: the address of the login account.

exchange_id
> The ID of the trading pair to be funded

token_id, quant
> TokenId and quantity (unit in base unit) of capital injection

When conducting a capital injection, depending on its quantity (quant), a proportion
of each token in the trading pair will be withdrawn from the account, and injected into the trading
pair. Depending on the difference in the balance of the transaction, the same amount of money for
the same token would vary.

### Transactions

    > exchangeTransaction [OwnerAddress] exchange_id token_id quant expected

OwnerAddress (optional)
> The address of the account which initiated the transaction.
> Default: the address of the login account.

exchange_id
> ID of the trading pair

token_id, quant
> The ID and quantity of tokens being exchanged, equivalent to selling

expected
> Expected quantity of another token

expected must be less than quant, or an error will be reported.

Example：

    > ExchangeTransaction 1 1000001 100 80

It is expected to acquire the 80 USDL by exchanging 1000001 from the trading pair ID of 1, and the amount is 100.(Equivalent to selling an amount of 100 tokenID - 1000001, at a price of 80 USDL, in trading pair ID - 1).

### Capital Withdrawal

    > exchangeWithdraw [OwnerAddress] exchange_id token_id quant

OwnerAddress (optional)
> The address of the account which initiated the transaction.
> Default: the address of the login account.

Exchange_id

> 
The ID of the trading pair to be withdrawn

Token_id, quant
> TokenId and quantity (unit in base unit) of capital withdrawal

When conducting a capital withdrawal, depending on its quantity (quant), a proportion of each token
in the transaction pair is withdrawn from the trading pair, and injected into the account. Depending on the difference in the balance of the transaction, the same amount of money for the same token would vary.

### Obtain information on trading pairs

ListExchanges
> List trading pairs

ListExchangesPaginated
> List trading pairs by page

## How to use the multi-signature feature of wallet-cli?

Multi-signature allows other users to access the account in order to better manage it. There are
three types of accesses:

- owner: access to the owner of account
- active: access to other features of accounts, and access that authorizes a certain feature. Block production authorization is not included if it's for witness purposes.
- witness: only for witness, block production authorization will be granted to one of the other users.

The rest of the users will be granted

```console
> Updateaccountpermission TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ \
{
  "owner_permission": {
    "type": 0,
    "permission_name": "owner",
    "threshold": 1,
    "keys": [
      {
        "address": "TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
        "weight": 1
      }
    ]
  },
  "witness_permission": {
    "type": 1,
    "permission_name": "owner",
    "threshold": 1,
    "keys": [
      {
        "address": "TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
        "weight": 1
      }
    ]
  },
  "active_permissions": [
    {
      "type": 2,
      "permission_name": "active12323",
      "threshold": 2,
      "operations": "7fff1fc0033e0000000000000000000000000000000000000000000000000000",
      "keys": [
        {
          "address": "TNhXo1GbRNCuorvYu5JFWN3m2NYr9QQpVR",
          "weight": 1
        },
        {
          "address": "TKwhcDup8L2PH5r6hxp5CQvQzZqJLmKvZP",
          "weight": 1
        }
      ]
    }
  ]
}
```

The account TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ gives the owner access to itself, active access to
TNhXo1GbRNCuorvYu5JFWN3m2NYr9QQpVR and TKwhcDup8L2PH5r6hxp5CQvQzZqJLmKvZP. Active access will
need signatures from both accounts in order to take effect.

If the account is not a witness, it's not necessary to set witness_permission, otherwise an error will occur.

### Signed transaction

    > SendCoin TJCnKsPa7y5okkXvQAidZBzqx3QyQ6sxMW 10000000000000000

Will show "Please confirm and input your permission id, if input y or Y means default 0, other
non-numeric characters will cancel transaction."

This will require the transfer authorization of active access. Enter: 2

Then select accounts and put in local password, i.e. TNhXo1GbRNCuorvYu5JFWN3m2NYr9QQpVR needs a
private key TNhXo1GbRNCuorvYu5JFWN3m2NYr9QQpVR to sign a transaction.

Select another account and enter the local password. i.e. TKwhcDup8L2PH5r6hxp5CQvQzZqJLmKvZP will
need a private key of TKwhcDup8L2PH5r6hxp5CQvQzZqJLmKvZP to sign a transaction.

The weight of each account is 1, threshold of access is 2. When the requirements are met, users
will be notified with “Send 10000000000000000 Sun to TJCnKsPa7y5okkXvQAidZBzqx3QyQ6sxMW
successful !!”.

This is how multiple accounts user multi-signature when using the same cli.
Use the instruction addTransactionSign according to the obtained transaction hex string if
signing at multiple cli. After signing, the users will need to broadcast final transactions
manually.

## Obtain weight information according to transaction

    > getTransactionSignWeight
    0a8c010a020318220860e195d3609c86614096eadec79d2d5a6e080112680a2d747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e5472616e73666572436f6e747261637412370a1541a7d8a35b260395c14aa456297662092ba3b76fc01215415a523b449890854c8fc460ab602df9f31fe4293f18808084fea6dee11128027094bcb8bd9d2d1241c18ca91f1533ecdd83041eb0005683c4a39a2310ec60456b1f0075b4517443cf4f601a69788f001d4bc03872e892a5e25c618e38e7b81b8b1e69d07823625c2b0112413d61eb0f8868990cfa138b19878e607af957c37b51961d8be16168d7796675384e24043d121d01569895fcc7deb37648c59f538a8909115e64da167ff659c26101

The information displays as follows:

```json
{
    "result":{
        "code":"PERMISSION_ERROR",
        "message":"Signature count is 2 more than key counts of permission : 1"
    },
    "permission":{
        "operations":"7fff1fc0033e0100000000000000000000000000000000000000000000000000",
        "keys":[
            {
                "address":"TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
                "weight":1
            }
        ],
        "threshold":1,
        "id":2,
        "type":"Active",
        "permission_name":"active"
    },
    "transaction":{
        "result":{
            "result":true
        },
        "txid":"7da63b6a1f008d03ef86fa871b24a56a501a8bbf15effd7aca635de6c738df4b",
        "transaction":{
            "signature":[
                "c18ca91f1533ecdd83041eb0005683c4a39a2310ec60456b1f0075b4517443cf4f601a69788f001d4bc03872e892a5e25c618e38e7b81b8b1e69d07823625c2b01",
                "3d61eb0f8868990cfa138b19878e607af957c37b51961d8be16168d7796675384e24043d121d01569895fcc7deb37648c59f538a8909115e64da167ff659c26101"
            ],
            "txID":"7da63b6a1f008d03ef86fa871b24a56a501a8bbf15effd7aca635de6c738df4b",
            "raw_data":{
                "contract":[
                    {
                        "parameter":{
                            "value":{
                                "amount":10000000000000000,
                                "owner_address":"TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
                                "to_address":"TJCnKsPa7y5okkXvQAidZBzqx3QyQ6sxMW"
                            },
                            "type_url":"type.googleapis.com/protocol.TransferContract"
                        },
                        "type":"TransferContract",
                        "Permission_id":2
                    }
                ],
                "ref_block_bytes":"0318",
                "ref_block_hash":"60e195d3609c8661",
                "expiration":1554123306262,
                "timestamp":1554101706260
            },
            "raw_data_hex":"0a020318220860e195d3609c86614096eadec79d2d5a6e080112680a2d747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e5472616e73666572436f6e747261637412370a1541a7d8a35b260395c14aa456297662092ba3b76fc01215415a523b449890854c8fc460ab602df9f31fe4293f18808084fea6dee11128027094bcb8bd9d2d"
        }
    }
}
```

### Get signature information according to transactions

    > getTransactionApprovedList
    0a8c010a020318220860e195d3609c86614096eadec79d2d5a6e080112680a2d747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e5472616e73666572436f6e747261637412370a1541a7d8a35b260395c14aa456297662092ba3b76fc01215415a523b449890854c8fc460ab602df9f31fe4293f18808084fea6dee11128027094bcb8bd9d2d1241c18ca91f1533ecdd83041eb0005683c4a39a2310ec60456b1f0075b4517443cf4f601a69788f001d4bc03872e892a5e25c618e38e7b81b8b1e69d07823625c2b0112413d61eb0f8868990cfa138b19878e607af957c37b51961d8be16168d7796675384e24043d121d01569895fcc7deb37648c59f538a8909115e64da167ff659c26101

```json
{
    "result":{

    },
    "approved_list":[
        "TKwhcDup8L2PH5r6hxp5CQvQzZqJLmKvZP",
        "TNhXo1GbRNCuorvYu5JFWN3m2NYr9QQpVR"
    ],
    "transaction":{
        "result":{
            "result":true
        },
        "txid":"7da63b6a1f008d03ef86fa871b24a56a501a8bbf15effd7aca635de6c738df4b",
        "transaction":{
            "signature":[
                "c18ca91f1533ecdd83041eb0005683c4a39a2310ec60456b1f0075b4517443cf4f601a69788f001d4bc03872e892a5e25c618e38e7b81b8b1e69d07823625c2b01",
                "3d61eb0f8868990cfa138b19878e607af957c37b51961d8be16168d7796675384e24043d121d01569895fcc7deb37648c59f538a8909115e64da167ff659c26101"
            ],
            "txID":"7da63b6a1f008d03ef86fa871b24a56a501a8bbf15effd7aca635de6c738df4b",
            "raw_data":{
                "contract":[
                    {
                        "parameter":{
                            "value":{
                                "amount":10000000000000000,
                                "owner_address":"TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
                                "to_address":"TJCnKsPa7y5okkXvQAidZBzqx3QyQ6sxMW"
                            },
                            "type_url":"type.googleapis.com/protocol.TransferContract"
                        },
                        "type":"TransferContract",
                        "Permission_id":2
                    }
                ],
                "ref_block_bytes":"0318",
                "ref_block_hash":"60e195d3609c8661",
                "expiration":1554123306262,
                "timestamp":1554101706260
            },
            "raw_data_hex":"0a020318220860e195d3609c86614096eadec79d2d5a6e080112680a2d747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e5472616e73666572436f6e747261637412370a1541a7d8a35b260395c14aa456297662092ba3b76fc01215415a523b449890854c8fc460ab602df9f31fe4293f18808084fea6dee11128027094bcb8bd9d2d"
        }
    }
}
```

## How to use smart contract

### deploy smart contracts

    > DeployContract [ownerAddress] contractName ABI byteCode constructor params isHex fee_limit consume_user_resource_percent origin_kandy_limit value token_value token_id(e.g: USDLTOKEN, use # if don't provided) <library:address,library:address,...> <lib_compiler_version(e.g:v5)> library:address,...>

OwnerAddress
> The address of the account that initiated the transaction, optional, default is the address of the login account.

contractName
> Name of smart contract

ABI
> Compile generated ABI code

byteCode
> Compile generated byte code

constructor, params, isHex
> Define the format of the bytecode, which determines the way to parse byteCode from parameters

fee_limit
> Transaction allows for the most consumed USDL

consume_user_resource_percent
> Percentage of user resource consumed, in the range [0, 100]

origin_kandy_limit
> The most amount of developer Kandy consumed by trigger contract once

value
> The amount of usdl transferred to the contract account

token_value
> Number of USDL10

token_id
> USDL10 Id

Example:

```
> deployContract normalcontract544 [{"constant":false,"inputs":[{"name":"i","type":"uint256"}],"name": "findArgsByIndexTest","outputs":[{"name":"z","type":"uint256"}],"payable":false,"stateMutability":"nonpayable","type":"function"}]
608060405234801561001057600080fd5b50610134806100206000396000f3006080604052600436106100405763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663329000b58114610045575b600080fd5b34801561005157600080fd5b5061005d60043561006f565b60408051918252519081900360200190f35b604080516003808252608082019092526000916060919060208201838038833901905050905060018160008151811015156100a657fe5b602090810290910101528051600290829060019081106100c257fe5b602090810290910101528051600390829060029081106100de57fe5b6020908102909101015280518190849081106100f657fe5b906020019060200201519150509190505600a165627a7a72305820b24fc247fdaf3644b3c4c94fcee380aa610ed83415061ff9e65d7fa94a5a50a00029 # # false 1000000000 75 50000 0 0 #
```

Get the result of the contract execution with the getTransactionInfoById command:

```console
> getTransactionInfoById 4978dc64ff746ca208e51780cce93237ee444f598b24d5e9ce0da885fb3a3eb9
{
    "id": "8c1f57a5e53b15bb0a0a0a0d4740eda9c31fbdb6a63bc429ec2113a92e8ff361",
    "fee": 6170500,
    "blockNumber": 1867,
    "blockTimeStamp": 1567499757000,
    "contractResult": [
        "6080604052600436106100405763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663329000b58114610045575b600080fd5b34801561005157600080fd5b5061005d60043561006f565b60408051918252519081900360200190f35b604080516003808252608082019092526000916060919060208201838038833901905050905060018160008151811015156100a657fe5b602090810290910101528051600290829060019081106100c257fe5b602090810290910101528051600390829060029081106100de57fe5b6020908102909101015280518190849081106100f657fe5b906020019060200201519150509190505600a165627a7a72305820b24fc247fdaf3644b3c4c94fcee380aa610ed83415061ff9e65d7fa94a5a50a00029"
    ],
    "contract_address": "TJMKWmC6mwF1QVax8Sy2AcgT6MqaXmHEds",
    "receipt": {
        "kandy_fee": 6170500,
        "kandy_usage_total": 61705,
        "net_usage": 704,
        "result": "SUCCESS"
    }
}
```

### trigger smart contarct

    > TriggerContract [ownerAddress] contractAddress method args isHex fee_limit value token_value token_id

OwnerAddress
> The address of the account that initiated the transaction, optional, default is the address of the login account.

contractAddress
> Smart contarct address

method
> The name of function and parameters, please refer to the example

args
> Parameter value

isHex
> The format of the parameters method and args, is hex string or not

fee_limit
> The most amount of usdl allows for the consumption

token_value
> Number of USDL10

token_id
> TRC10 id, If not, use ‘#’ instead

Example:

```console
> triggerContract TGdtALTPZ1FWQcc5MW7aK3o1ASaookkJxG findArgsByIndexTest(uint256) 0 false
1000000000 0 0 #
# Get the result of the contract execution with the getTransactionInfoById command
> getTransactionInfoById 7d9c4e765ea53cf6749d8a89ac07d577141b93f83adc4015f0b266d8f5c2dec4
{
    "id": "de289f255aa2cdda95fbd430caf8fde3f9c989c544c4917cf1285a088115d0e8",
    "fee": 8500,
    "blockNumber": 2076,
    "blockTimeStamp": 1567500396000,
    "contractResult": [
        ""
    ],
    "contract_address": "TJMKWmC6mwF1QVax8Sy2AcgT6MqaXmHEds",
    "receipt": {
        "kandy_fee": 8500,
        "kandy_usage_total": 85,
        "net_usage": 314,
        "result": "REVERT"
    },
    "result": "FAILED",
    "resMessage": "REVERT opcode executed"
}
```

### get details of a smart contract

    > GetContract contractAddress

contractAddress
> smart contract address

Example:

```console
> GetContract TGdtALTPZ1FWQcc5MW7aK3o1ASaookkJxG
{
    "origin_address": "TRGhNNfnmgLegT4zHNjEqDSADjgmnHvubJ",
    "contract_address": "TJMKWmC6mwF1QVax8Sy2AcgT6MqaXmHEds",
    "abi": {
        "entrys": [
            {
                "name": "findArgsByIndexTest",
                "inputs": [
                    {
                        "name": "i",
                        "type": "uint256"
                    }
                ],
                "outputs": [
                    {
                        "name": "z",
                        "type": "uint256"
                    }
                ],
                "type": "Function",
                "stateMutability": "Nonpayable"
            }
        ]
    },
    "bytecode": "608060405234801561001057600080fd5b50610134806100206000396000f3006080604052600436106100405763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663329000b58114610045575b600080fd5b34801561005157600080fd5b5061005d60043561006f565b60408051918252519081900360200190f35b604080516003808252608082019092526000916060919060208201838038833901905050905060018160008151811015156100a657fe5b602090810290910101528051600290829060019081106100c257fe5b602090810290910101528051600390829060029081106100de57fe5b6020908102909101015280518190849081106100f657fe5b906020019060200201519150509190505600a165627a7a72305820b24fc247fdaf3644b3c4c94fcee380aa610ed83415061ff9e65d7fa94a5a50a00029",
    "consume_user_resource_percent": 75,
    "name": "normalcontract544",
    "origin_kandy_limit": 50000,
    "code_hash": "23423cece3b4866263c15357b358e5ac261c218693b862bcdb90fa792d5714e6"
}
```

### update smart contract parameters

    > UpdateKandyLimit [ownerAddress] contract_address kandy_limit  # Update parameter kandy_limit
    > UpdateSetting [ownerAddress] contract_address consume_user_resource_percent  # Update parameter consume_user_resource_percent

## How to delegate resource

### delegate resource

    > freezeBalance [OwnerAddress] frozen_balance frozen_duration [ResourceCode:0 BANDWIDTH, 1 USDL_POWER] [receiverAddress]

The latter two parameters are optional parameters. If not set, the USDL is frozen to obtain
resources for its own use; if it is not empty, the acquired resources are used by receiverAddress.

OwnerAddress
> The address of the account that initiated the transaction, optional, default is the address of the login account.

frozen_balance
> The amount of frozen USDL, the unit is the smallest unit (Sun), the minimum is 1000000sun.

frozen_duration
> frezen duration, 3 days

ResourceCode
> 0 BANDWIDTH;1 USDL_POWER

receiverAddress
> target account address

### unfreeze delegated resource

    > unfreezeBalance [OwnerAddress] ResourceCode(0 BANDWIDTH, 1 CPU) [receiverAddress]

The latter two parameters are optional. If they are not set, the BANDWIDTH resource is unfreeze
by default; when the receiverAddress is set, the delegate resources are unfreezed.

### get resource delegation information

getDelegatedResource fromAddress toAddress
> get the information from the fromAddress to the toAddress resource delegate

getDelegatedResourceAccountIndex address
> get the information that address is delegated to other account resources

## Wallet related commands

**RegisterWallet**
> Register your wallet, you need to set the wallet password and generate the address and private key.

**BackupWallet**
> Back up your wallet, you need to enter your wallet password and export the private key.hex string format, such
as: 721d63b074f18d41c147e04c952ec93467777a30b6f16745bc47a8eae5076545

**BackupWallet2Base64**
> Back up your wallet, you need to enter your wallet password and export the private key.base64 format, such as: ch1jsHTxjUHBR+BMlS7JNGd3ejC28WdFvEeo6uUHZUU=

**ChangePassword**
> Modify the password of an account

**ImportWallet**
> Import wallet, you need to set a password, hex String format

**ImportWalletByBase64**
> Import wallet, you need to set a password, base64 fromat

## Account related commands

**GenerateAddress**
> Generate an address and print out the public and private keys

**GetAccount**
> Get account information based on address

**GetAccountNet**
> The usage of bandwidth

**GetAccountResource**
> The usage of bandwidth and kandy

**GetAddress**
> Get the address of the current login account

**GetBalance**
> Get the balance of the current login account

## How to get transaction information

**GetTransactionById**
> Get transaction information based on transaction id

**GetTransactionCountByBlockNum**
> Get the number of transactions in the block based on the block height

**GetTransactionInfoById**
> Get transaction-info based on transaction id, generally used to check the result of a smart contract trigger

**GetTransactionInfoByBlockNum**
> Get the list of transaction information in the block based on the block height

## How to get block information

**GetBlock**
> Get the block according to the block number; if you do not pass the parameter, get the latest block

**GetBlockById**
> Get block based on blockID

**GetBlockByLatestNum n**
> Get the latest n blocks, where 0 < n < 100

**GetBlockByLimitNext startBlockId endBlockId**
> Get the block in the range [startBlockId, endBlockId)

## Some others

**GetNextMaintenanceTime**
> Get the start time of the next maintain period

**ListNodes**
> Get other peer information

**ListWitnesses**
> Get all miner node information

**BroadcastTransaction**
> Broadcast the transaction, where the transaction is in hex string format.


## How to transfer shielded TRC20 token

If you want to try to transfer shielded TRC20 token, you'd better set the `blockNumberStartToScan` field in `config.conf` file.
This field is used to set the starting block that the wallet needs to scan. If you ignore this field, or set it to 0, 
the notes you receive will probably take a long time to show up in the wallet. It is recommended that this field is 
set to the block number in which the earliest relevant shielded contract was created. If the exact number is not known, 
this field can be set as follows. If used in mainnet, please set 22690588. If used in Nile testnet, please set 6380000. 
Otherwise, please set 0.

When you begin to transfer TRC20 token to shielded address, you must have a shielded address. The
 following commands help to generate shielded account.

### GetSpendingKey

Generate a sk

Example:

```console
> GetSpendingKey
0eb458b309fa544066c40d80ce30a8002756c37d2716315c59a98c893dbb5f6a
```

### GetExpandedSpendingKey

```console
> GetExpandedSpendingKey sk
```
Generate ask, nsk, ovk from sk

Example:

```console
> GetExpandedSpendingKey 0eb458b309fa544066c40d80ce30a8002756c37d2716315c59a98c893dbb5f6a
ask:252a0f6f6f0bac114a13e1e663d51943f1df9309649400218437586dea78260e
nsk:5cd2bc8d9468dbad26ea37c5335a0cd25f110eaf533248c59a3310dcbc03e503
ovk:892a10c1d3e8ea22242849e13f177d69e1180d1d5bba118c586765241ba2d3d6
```

### GetAkFromAsk

```console
> GetAkFromAsk ask
```
Generate ak from ask

Example:

```console
> GetAkFromAsk 252a0f6f6f0bac114a13e1e663d51943f1df9309649400218437586dea78260e
ak:f1b843147150027daa5b522dd8d0757ec5c8c146defd8e01b62b34cf917299f1
```

### GetNkFromNsk

```console
> GetNkFromNsk nsk
```
Generate nk from nsk

Example:

```console
> GetNkFromNsk 5cd2bc8d9468dbad26ea37c5335a0cd25f110eaf533248c59a3310dcbc03e503
nk:ed3dc885049f0a716a4de8c08c6cabcad0da3c437202341aa3d9248d8eb2b74a
```

### GetIncomingViewingKey

```console
> GetIncomingViewingKey ak[64] nk[64]
```
Generate ivk from ak and nk

Example:

```console
> Getincomingviewingkey f1b843147150027daa5b522dd8d0757ec5c8c146defd8e01b62b34cf917299f1
 ed3dc885049f0a716a4de8c08c6cabcad0da3c437202341aa3d9248d8eb2b74a
ivk:148cf9e91f1e6656a41dc9b6c6ee4e52ff7a25b25c2d4a3a3182d0a2cd851205
```

### GetDiversifier

Generate a diversifier

Example:

```console
> GetDiversifier
11db4baf6bd5d5afd3a8b5
```

### GetShieldedPaymentAddress

```console
> GetShieldedPaymentAddress ivk[64] d[22]
```
Generate a shielded address from ivk and d

### SetShieldedTRC20ContractAddress

```console
> SetShieldedTRC20ContractAddress TRC20ContractAddress ShieldedContractAddress
```
TRC20ContractAddress
> TRC20 contract address

ShieldedContractAddress
> Shielded contract address

Set TRC20 contract address and shielded contract address. Please execute this command before you perform all the following operations related to the shielded transaction of TRC20 token except `ScanShieldedTRC20NoteByIvk` and `ScanShieldedTRC20NoteByOvk`.

When you execute this command, the `Scaling Factor` will be shown. The `Scaling Factor` is set in
 the shielded contract. 



Example:

```console
> SetShieldedTRC20ContractAddress TLDxNTzNvEPd4gHox8V1zK2w82LFnideKE TKERuAmhJh8vZi1dzJtx8926xeCT74747e
scalingFactor():ed3437f8
SetShieldedTRC20ContractAddress succeed!
The Scaling Factor is 1000
That means:
No matter you MINT, TRANSFER or BURN, the value must be an integer multiple of 1000
```

### LoadShieldedTRC20Wallet

Load TRC20 shielded address, shielded note and start to scan by ivk.

Example:

```console
> LoadShieldedTRC20Wallet
Please input your password for shieldedTRC20 wallet.
> *******
LoadShieldedTRC20Wallet successful !!!
```
