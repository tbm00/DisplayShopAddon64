# DisplayShopAddon64
A spigot plugin that expands DisplayShops and adds user-friendly, but powerful GUIs.

Created by tbm00 for play.mc64.wtf.


## Dependencies
- **Java 17+**: REQUIRED
- **Spigot 1.18.1+**: UNTESTED ON OLDER VERSIONS
- **DisplayShops**: REQUIRED
- **Vault**: REQUIRED
- **Rep64**: REQUIRED


## Commands
#### Shopper Commands 
- testshop
  - `/testshop` Open shop category GUI
  - `/testshop [pog/blocks/food/drops/ores]` Open shop category GUI with specific landing
  - `/testshop [item/player]` Open GUI with all shops matching player/item
- buy
  - `/buy` Open shop category GUI
  - `/buy [pog/blocks/food/drops/ores]` Open shop category GUI with specific landing
  - `/buy [item/player]` Open GUI with all shops matching player/item
- sell
  - `/sell` Open shop category GUI
  - `/sell [pog/blocks/food/drops/ores]` Open shop category GUI with specific landing
  - `/sell [item/player]` Open GUI with all shops matching player/item

#### Shop Owner Commands
- `/testshop help` Display help menu
- `/testshop buy <#>` Buy shop creation item(s)
- `/testshop list` Open your shop list & manage GUI
- `/testshop advertise` Broadcast the shop you're looking at
- `/testshop store-inv` Deposit all appicable items from your inv into your shops
- `/testshop deposit-all max/<#>` Deposit money into all your shops
- `/testshop withdraw-all max/<#>` Withdraw money from all your shops


## Permissions
#### Player Permissions
- `displayshopaddon64.player` Ability to use /testshop, /buy, & /sell *(default: everyone)*

#### Admin Permissions
- `displayshopaddon64.admin` Ability to use __________ *(default: op)*


## Config
```
# DisplayShopAddon64 v0.0.3-beta by @tbm00
# https://github.com/tbm00/DisplayShopAddon64

enabled: true

lang:
  prefix: "&8[&fShop&8] &7"

feature:
  enabled: true
  dsMaxStoredBalance: 20000000
  dsMaxStoredStock: 8192
  guiDefaultCategory: "shoppog"
  dsDescChange: true
```