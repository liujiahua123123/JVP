function writeLib(obj) {
    obj.defineProperty(Object.prototype, 'getValue', {
        value: function () {
            return UI.GetValue(this);
        },
        enumerable: false
    });
    obj.defineProperty(Object.prototype, 'getColor', {
        value: function () {
            return UI.GetColor(this);
        },
        enumerable: false
    });
    obj.defineProperty(Object.prototype, 'getString', {
        value: function () {
            return UI.GetString(this);
        },
        enumerable: false
    });
    obj.defineProperty(Object.prototype, 'setValue', {
        value: function (a) {
            return UI.SetValue(this, a);
        },
        enumerable: false
    });
    obj.defineProperty(Object.prototype, 'setColor', {
        value: function (a) {
            return UI.SetColor(this, a);
        },
        enumerable: false
    });
    obj.defineProperty(Object.prototype, 'setString', {
        value: function (a) {
            return UI.SetString(this, a);
        },
        enumerable: false
    });
    obj.defineProperty(Object.prototype, 'setVisible', {
        value: function (a) {
            return UI.SetEnabled(this, a?1:0);
        },
        enumerable: false
    });
}
writeLib(Object)

/**
 *
 * DATA(s)
 *
 */

const ITEM_TO_ICON_ID = {
    "weapon_null" : "",
    "weapon_deagle": "A",
    "weapon_elite": "B",
    "weapon_fiveseven": "C",
    "weapon_glock": "D",
    "weapon_ak47": "W",
    "weapon_aug": "U",
    "weapon_awp": "Z",
    "weapon_famas": "R",
    "weapon_m249": "g",
    "weapon_g3sg1": "X",
    "weapon_galilar": "Q",
    "weapon_m4a1": "S",
    "weapon_m4a1_silencer": "T",
    "weapon_mac10": "K",
    "weapon_hkp2000": "E",
    "weapon_mp5sd": "N",
    "weapon_ump45": "L",
    "weapon_xm1014": "b",
    "weapon_bizon": "M",
    "weapon_mag7": "d",
    "weapon_negev": "f",
    "weapon_sawedoff": "c",
    "weapon_tec9": "H",
    "weapon_taser": "h",
    "weapon_p250": "F",
    "weapon_mp7": "N",
    "weapon_mp9": "O",
    "weapon_nova": "e",
    "weapon_p90": "P",
    "weapon_scar20": "Y",
    "weapon_sg556": "V",
    "weapon_ssg08": "a",
    "weapon_flashbang": "i",
    "weapon_hegrenade": "j",
    "weapon_smokegrenade": "k",
    "weapon_molotov": "l",
    "weapon_decoy": "m",
    "weapon_incgrenade": "n",
    "weapon_usp_silencer": "G",
    "weapon_cz75a": "I",
    "weapon_revolver": "J",
    "item_assaultsuit": "p",
    "item_kevlar": "q",
    "item_defuser": "r"
};


const PRIMARY_WEAPON_TO_ICON = {
    "weapon_ak47": "W",
    "weapon_aug": "U",
    "weapon_awp": "Z",
    "weapon_famas": "R",
    "weapon_m249": "g",
    "weapon_g3sg1": "X",
    "weapon_galilar": "Q",
    "weapon_m4a1": "S",
    "weapon_m4a1_silencer": "T",
    "weapon_mac10": "K",
    "weapon_mp5sd": "N",
    "weapon_ump45": "L",
    "weapon_xm1014": "b",
    "weapon_bizon": "M",
    "weapon_mag7": "d",
    "weapon_negev": "f",
    "weapon_sawedoff": "c",
    "weapon_mp7": "N",
    "weapon_mp9": "O",
    "weapon_nova": "e",
    "weapon_p90": "P",
    "weapon_scar20": "Y",
    "weapon_sg556": "V",
    "weapon_ssg08": "a",
}

const SECONDARY_WEAPON_TO_ICON = {
    "weapon_deagle": "A",
    "weapon_elite": "B",
    "weapon_fiveseven": "C",
    "weapon_glock": "D",
    "weapon_hkp2000": "E",
    "weapon_tec9": "H",
    "weapon_p250": "F",
    "weapon_usp_silencer": "G",
    "weapon_cz75a": "I",
    "weapon_revolver": "J",
}


UI["AddSubTab"](["Config", "SUBTAB_MGR"], "BESTKIM [buylog]")
var headerColorPicker = UI.AddColorPicker(["Config", "BESTKIM [buylog]", "BESTKIM [buylog]"],"Header Color")
var bodyColorPicker = UI.AddColorPicker(["Config", "BESTKIM [buylog]", "BESTKIM [buylog]"],"Body Color")

var headerColor = {
    r: 10,g:10,b:10,a:10,increase:3
}

var bodyColor = {
    r: 10,g:10,b:10,a:10,increase:3
}


function breathColor(color){
    color.a += color.increase
    if(color.a > 255){
        color.increase = -3;
        color.a = 255
    }
    if(color.a < 0){
        color.increase = 3;
        color.a = 0;
    }
}


var enemyData = {}

function onPlayerConnectFull() {
    enemyData = {}
}
Cheat.RegisterCallback("player_connect_full", "onPlayerConnectFull");

function onEnemyPurchase(){
    var buyer = Event.GetInt("userid")

    if(!Entity.IsEnemy(Entity.GetEntityFromUserID(buyer))){
        return;
    }
    var item = Event.GetString("weapon");
    if(ITEM_TO_ICON_ID[item] === undefined){
        Cheat.Print(buyer + " buying " + item + " which has undefined icon")
        return;
    }

    if(enemyData[buyer] === undefined){
        enemyData[buyer] = {
            extra: "",
            primary: "weapon_null",
            secondary: "weapon_null",
        }
    }

    var isPrimaryWeapon = PRIMARY_WEAPON_TO_ICON[item] !== undefined
    var isSecondaryWeapon = SECONDARY_WEAPON_TO_ICON[item] !== undefined


    if(isPrimaryWeapon){
        enemyData[buyer]["primary"] = item
        return;
    }

    if(isSecondaryWeapon){
        enemyData[buyer]["secondary"] = item
        return;
    }


    enemyData[buyer]["extra"] += ITEM_TO_ICON_ID[item] + " "
}

function onPlayerDeath(){
    var uid = Event.GetInt("userid");

    if(enemyData[uid] !== undefined){
        enemyData[uid]["primary"] =  "weapon_null"
        enemyData[uid]["secondary"] =  "weapon_null"
        enemyData[uid]["extra"] =  ""
    }

}

var sandbox = {
    x: 100,
    y: 100,
    width: 300,
    height: 200,
}


function renderArc(x, y, radius, radius_inner, start_angle, end_angle, segments_raw, color)
{
    segments_raw *= 7

    for (var i = start_angle; i < start_angle + end_angle; i = i + segments_raw)
    {

        var rad = i * Math.PI / 180;
        var rad2 = (i + segments_raw) * Math.PI / 180;

        var rad_cos = Math.cos(rad);
        var rad_sin = Math.sin(rad);

        var rad2_cos = Math.cos(rad2);
        var rad2_sin = Math.sin(rad2);

        var x1_inner = x + rad_cos * radius_inner;
        var y1_inner = y + rad_sin * radius_inner;

        var x1_outer = x + rad_cos * radius;
        var y1_outer = y + rad_sin * radius;

        var x2_inner = x + rad2_cos * radius_inner;
        var y2_inner = y + rad2_sin * radius_inner;

        var x2_outer = x + rad2_cos * radius;
        var y2_outer = y + rad2_sin * radius;

        Render.Polygon( [
                [ x1_outer, y1_outer ],
                [ x2_outer, y2_outer ],
                [ x1_inner, y1_inner ] ],
            color
        );

        Render.Polygon( [
                [ x1_inner, y1_inner ],
                [ x2_outer, y2_outer ],
                [ x2_inner, y2_inner ] ],
            color
        );
    }
}


function filledRoundRect(x, y, w, h, r, color, headerColor) {
    if(headerColor === undefined){
        headerColor = color
    }


    Render.FilledRect(x + r-29, y+24, w - 2*r+59 , r-11, color); //半园中top 2
    Render.FilledRect(x + r-19, y+13, w - 2*r+39 , r-19, headerColor); //半园中top
    Render.FilledRect(x + r -19, y + h - r-7 , w - 2*r+39, r-20, color); //半园中down


    renderArc(x + r-19.5, y + r-6.5, r-19.5, 0 ,180,90,1,headerColor)//top左边半园 
    renderArc(x - r + w +19.5, y + r-6.5, r-19.5, 0,270,90,1,headerColor) //top又边半园 

    renderArc(x + r-19.5, y - r-7.5 + h, r-20, 0,90,90,1,color)  //down左边半园 
    renderArc(x - r+ w+19.5, y - r-7.5 + h, r-20, 0,0,90,1,color) //down又边半园 

    Render.FilledRect(x+1, y+15 + r, w-1 , h - 2*r-22, color); //mid 中
}


var basicFont = null;
var iconFont = null;
var lastMouseX = -1;
var lastMouseY = -1;

var path = ["Visuals", "Extra", "Removals"];

const storageX = UI.AddSliderInt(path, "SKIM_XXX", 0, Global.GetScreenSize()[0]);
const storageY = UI.AddSliderInt(path, "SKIM_YYY", 0, Global.GetScreenSize()[1]);
function in_bounds(_0x13a91c, _0xad477a, _0x23ee52, _0x286f77, _0x565bf4) {
    return _0x13a91c[0x0] > _0xad477a && _0x13a91c[0x1] > _0x23ee52 && _0x13a91c[0x0] < _0x286f77 && _0x13a91c[0x1] < _0x565bf4;
}


function onDraw(){
    if(basicFont == null){
        basicFont = Render["GetFont"]("segoeui.ttf", 12, true)
        iconFont = Render["GetFont"]("undefeated.ttf", 15, false)
    }
    //refine sandbox size
    var maxText = ""
    var maxTextLength = 0
    Object.keys(enemyData).forEach(function (key) {
        var entity = Entity.GetEntityFromUserID(key)
        var entityName = Entity.GetName(entity) + ""
        if(entityName.length > maxTextLength){
            maxTextLength = entityName.length
            maxText = entityName
        }
    })

    var playerCounts = 2
    var keys = Object.keys(enemyData)
    if(keys.length > 2){
        playerCounts = keys.length
    }

    var heightPerPlayer = 38
    var heightForHeading = 90

    var widthForName = 60;
    var widthForPrimary = 80
    var widthForSecondary = 60
    var widthForExtra = 140
    var widthForMoney = 60
    var widthForArmor = 60

    var widthForMaxName = Render.TextSize(maxText,basicFont)[0]
    if(widthForMaxName> 60){
        widthForName = widthForMaxName
    }


    sandbox.width = widthForName + widthForPrimary + widthForSecondary + widthForExtra + widthForMoney + widthForArmor
    sandbox.height = heightForHeading + (heightPerPlayer*playerCounts)
    filledRoundRect(sandbox.x,sandbox.y,sandbox.width,sandbox.height,30,bodyColorPicker.getColor(),headerColorPicker.getColor())


    sandbox.x = storageX.getValue()
    sandbox.y = storageY.getValue()

    if (Global.IsKeyPressed(1) && UI.IsMenuOpen()) {
        const mouse_pos = Global.GetCursorPosition();
        if (in_bounds(mouse_pos, sandbox.x, sandbox.y, sandbox.x + sandbox.x + sandbox.width, sandbox.y + sandbox.height)) {
            if (lastMouseX === -1) {
                lastMouseX = mouse_pos[0]
                lastMouseY = mouse_pos[1]
            } else {
                var xOffset = mouse_pos[0] - lastMouseX
                var yOffset = mouse_pos[1] - lastMouseY

                sandbox.x += xOffset
                sandbox.y += yOffset

                storageX.setValue(sandbox.x)
                storageY.setValue(sandbox.y)

                lastMouseX = mouse_pos[0]
                lastMouseY = mouse_pos[1]
            }
        } else {
            lastMouseX = -1
            lastMouseY = -1
        }
    }else{
        lastMouseX = -1
        lastMouseY = -1
    }



    //Render.String(sandbox.x + sandbox.width/2,sandbox.y + 10,1,"BuyLog",[255,255,255,200],basicFont)



    var accumulatedX = sandbox.x
    var accumulatedY = sandbox.y + 45


    //TH
    Render.String(accumulatedX + widthForName/2,accumulatedY,1,"Name",[255,255,255,255],basicFont)
    Render.String(accumulatedX + widthForName/2 + 200,accumulatedY-20,1,"Buy Log",[255,255,255,255],basicFont)
    accumulatedX += widthForName
    var weaponSlotWidth = widthForPrimary + widthForSecondary + widthForExtra
    Render.String(accumulatedX + weaponSlotWidth/2-100,accumulatedY,1,"Weapons",[255,255,255,255],basicFont)
    accumulatedX += weaponSlotWidth
    Render.String(accumulatedX + widthForMoney/2,accumulatedY,1,"Money",[255,255,255,255],basicFont)
    accumulatedX += widthForMoney
    Render.String(accumulatedX + widthForArmor/2,accumulatedY,1,"Armor",[255,255,255,255],basicFont)
    accumulatedX += widthForArmor
    accumulatedY += 18
    //TH
    //TB
    Object.keys(enemyData).forEach(function (userId) {
        var entity = Entity.GetEntityFromUserID(userId)
        var entityName = "" + Entity.GetName(entity)
        var armorStatus = Entity.IsAlive(entity)?"-":Entity.GetProp(entity,'CCSPlayerResource', 'm_iArmor')>0?"true":"-"
        var money = Entity.GetProp(entity, "CCSPlayer", "m_iAccount")
        var data = enemyData[userId]

        /**
         * template
        {
            extra: "",
            primary: "weapon_null",
            secondary: "weapon_null",
        }
         */

        accumulatedY += 18
        accumulatedX = sandbox.x
        Render.String(accumulatedX + widthForName/2,accumulatedY,1,entityName,[255,255,255,255],basicFont)
        accumulatedX += widthForName
        Render.String(accumulatedX + widthForPrimary/2,accumulatedY,1,ITEM_TO_ICON_ID[data["primary"]],[255,255,255,255],iconFont)
        accumulatedX += widthForPrimary
        Render.String(accumulatedX + widthForSecondary/2,accumulatedY,1,ITEM_TO_ICON_ID[data["secondary"]],[255,255,255,255],iconFont)
        accumulatedX += widthForSecondary
        Render.String(accumulatedX,accumulatedY,0,data["extra"],[255,255,255,255],iconFont)
        accumulatedX += widthForExtra
        Render.String(accumulatedX + widthForMoney/2,accumulatedY,1,"$" + money,[255,255,255,255],basicFont)
        accumulatedX += widthForMoney
        Render.String(accumulatedX + widthForArmor/2,accumulatedY,1,armorStatus,[255,255,255,255],basicFont)
        accumulatedX += widthForArmor

        accumulatedY += 18
    })
}



Cheat.RegisterCallback("Draw", "onDraw");
Cheat.RegisterCallback("item_purchase", "onEnemyPurchase");
Cheat.RegisterCallback("player_death", "onPlayerDeath");