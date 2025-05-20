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
- shop
  - `/shop` Open shop category GUI
  - `/shop [pog/blocks/food/drops/ores]` Open shop category GUI with specific landing
  - `/shop <item/player>` Open GUI with all shops matching player/item
- buy
  - `/buy` Open shop category GUI
  - `/buy [pog/blocks/food/drops/ores]` Open shop category GUI with specific landing
  - `/buy <item/player>` Open GUI with all buy-able shops matching player/item
- sell
  - `/sell` Open shop category GUI
  - `/sell [pog/blocks/food/drops/ores]` Open shop category GUI with specific landing
  - `/sell <item/player>` Open GUI with all sell-able shops matching player/item
  - `/sell inv <#>` Sell all items in your inv for a minimum of $<#> each
  - `/sell gui <#>` Open a GUI and sell items for a minimum of $<#> each
- sellinv - alias: sellall
  - `/sellinv <#>` Sell all items in your inv for a minimum of $<#> each
- sellgui - alias: sellg, testsg
  - `/sellgui <#>` Open a GUI and sell items for a minimum of $<#> each

#### Shop Owner Commands
- `/shop help` Display help menu
- `/shop buy <#>` Buy shop creation item(s)
- `/shop list` Open your shop list & manage GUI
- `/shop advertise` Broadcast the shop you're looking at
- `/shop store-inv` Deposit all appicable items from your inv into your shops
- `/shop deposit-all max/<#>` Deposit money into all your shops
- `/shop withdraw-all max/<#>` Withdraw money from all your shops

#### Admin Commands
- `/shopsadmin` View/manage all shops
- `/shopsadmin <player/item>` View/manage all <item/player> shops
- `/shopsadmin transfer <playerFrom> <playerTo>` Change shops' owner
- `/shopsadmin [pos1/pos2/copy]` Set copy positions
- `/shopsadmin paste` Set paste position & paste

## Permissions
#### Player Permissions
- `displayshopaddon64.player` Ability to use player commands *(default: everyone)*
- `displayshopaddon64.player.move-money` Ability to store money into shops with command *(default: everyone)*
- `displayshopaddon64.player.store-inv` Ability to store inv items into shops with command *(default: everyone)*
- `displayshopaddon64.player.sell-inv` Ability to sell inv items with command *(default: everyone)*
- `displayshopaddon64.player.sell-gui` Ability to open sell GUI with command *(default: everyone)*

#### Admin Permissions
- `displayshopaddon64.admin` Ability to use admin commands *(default: op)*


## Config
```
# DisplayShopAddon64 v0.0.8-beta by @tbm00
# https://github.com/tbm00/DisplayShopAddon64

enabled: true

lang:
  prefix: "&8[&fShop&8] &7"

feature:
  enabled: true
  dsMaxStoredBalance: 20000000
  dsMaxStoredStock: 8192
  guiDefaultCategory: "shopgui"
  dsDescChange: true
```