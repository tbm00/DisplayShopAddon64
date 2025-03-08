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
- testbuy
  - `/testbuy` Open shop category GUI
  - `/testbuy [pog/blocks/food/drops/ores]` Open shop category GUI with specific landing
  - `/testbuy [item/player]` Open GUI with all buy-able shops matching player/item
- testsell
  - `/testsell` Open shop category GUI
  - `/testsell [pog/blocks/food/drops/ores]` Open shop category GUI with specific landing
  - `/testsell [item/player]` Open GUI with all sell-able shops matching player/item
  - `/testsell inv <#>` Sell all items in your inv for a minimum of $<#> each
  - `/testsell gui <#>` Open a GUI and sell items for a minimum of $<#> each
- testsellinv - alias: testsellall
  - `/testsellinv <#>` Sell all items in your inv for a minimum of $<#> each
- testsellgui - alias: testsellg, testsg
  - `/testsellgui <#>` Open a GUI and sell items for a minimum of $<#> each

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
- `displayshopaddon64.player` Ability to use player commands *(default: everyone)*

#### Admin Permissions
- `displayshopaddon64.admin` Ability to use __________ *(default: op)*


## Config
```
# DisplayShopAddon64 v0.0.4-beta by @tbm00
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