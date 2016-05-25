Program MakeAndSell;
{
Autor: Half-Life;
Description: Скрипт берёт из контейнера который находится дома металл,
летит к кузне, делает щиты, продаёт и скидывает деньги в банк.
Тестировался на шарде UOAwakening;
UOStealthClientVersion: 7.0.3;
Warning! Будьте бдительны! - Администрация многих игровых серверов враждебно
относится к использованию стелс клиента на своих серверах.
Заподозрив вас в использовании стелс клиента и других неправославных программ
они начинают сатанеть и в порыве слепой ярости могут попасть по вам Банхаммером;
}
const
VendorID = $0003022E; //ID Вендора которому будем продавать.
VendorContextMenu = 4; //Индекс контекстного меню отвечающего за продажу.

RuneBook = $400B684A; //ID Рб.
RuneBookGumpID = $554B87F3; //ID гампа Рб.
RecallMethod = 7; //Через что реколиться. 5 - магия, 7 - чива.

HomeRune = 2; // Номер рунки к дому. Отсчёт начинается с единицы.
HomeX = 2351; // Координата Х дома.
HomeY = 783; // Координата У дома.

ForgeRune = 5; // Номер рунки к форджу. Отсчёт начинается с единицы.
ForgeX = 977; // Координата Х форджа.
ForgeY = 510; // Координата У форджа.

VendorRune = 5; // Номер рунки к вендору. Отсчёт начинается с единицы.
VendorX = 977;  // Координата Х вендора.
VendorY = 510;  // Координата У вендора.

BankRune = 1; // Номер рунки к банку. Отсчёт начинается с единицы.
BankX = 986; // Координата Х банка.
BankY = 517; // Координата У банка.

GoldType = $0EED; // Тип голда.

IngotsStorage = $4010F755; // ID Сумки с металлом которая находится в доме.
IngotsType = $1BF2; // Тип инготов.
IngotsColor = $0000; // Цвет инготов.
IngotsAmount = 370; // Количество инготов необходимое для работы скрипта.

ShieldType = $1B76;  // Тип щита. Heater shield
ShieldAmount = 20;   // Количество щитов которое будем ковать.

BlacksmithType = $0FBB; // Тип бс тулзы (tongs).
TinkerType = $1EB8; // Тип тинкер тулзы.
ToolsGumpID = $38920ABD; // ID гампа тулз.

TKNumFirst = '15'; // Первая кнопка в тинкер меню отвечающая за меню тузл.
TKNumSecond = '23'; // Вторая кнопка в тинкер меню отвечающая за крафт тиинкер тулзы.
TKBSToolsNum = '86'; // Вторая кнопка в тинкер меню отвечающая за крафт бс тулзы.

BSNumFirst = '15'; // Первая кнопка в бс меню отвечающая за меню щитов.
BSNumSecond = '16'; // Вторая кнопка в бс меню отвечающая за крафт щита.

WaitRecalTime = 2000;
WaitTime = 500;
WaitLagTime = 10000;

var
GoldTotal:Integer = 0;

//==============================================================================
//# Utils
//==============================================================================

procedure WaitLag(WaitTime,LagTime:Word);
begin
  Wait(WaitTime);
  CheckLag(LagTime);
end;

procedure CloseGumps;
begin
  while IsGump do begin
    if not Connected then Exit;
    if not IsGumpCanBeClosed(GetGumpsCount-1) then begin
      WaitGump('0');
      Exit;
    end;
    CloseSimpleGump(GetGumpsCount-1);
  end;
end;

//==============================================================================
//# Initial
//==============================================================================

procedure Initial;
begin
  ClearSystemJournal;
  ClearJournal;
  IgnoreReset;
  AddToSystemJournal('Стартуем!');
  AddToSystemJournal('Закрываем гапмы...');
  CloseGumps;
  AddToSystemJournal('Гампы закрыты.');
end;

//==============================================================================
//# CheckStates
//==============================================================================

procedure CheckConnection;
begin
  if Connected then Exit;
  AddToSystemJournal('Нет коннекта.');
  while not Connected do begin
    AddToSystemJournal('Пытаюсь зайти на сервер...');
    Wait(5000);
  end;
  AddToSystemJournal('Есть коннект.');
  Initial;
end;

procedure CheckDead;
begin
  if not Dead then Exit;
  AddToSystemJournal('Очень жаль, но Вы умерли. Скрипт остановлен.');
  Halt;
end;

procedure CheckStates;
begin
  CheckConnection;
  CheckDead;
end;

//==============================================================================
//# Recalling
//==============================================================================

procedure RecallTo(Destination:Byte;X,Y:Word;Place:String);
begin
  AddToSystemJournal('Пытаюсь среколиться в '+Place);
  while (GetX(Self)<>X) and (GetY(Self)<>Y) do begin
    CheckStates;
    UseObject(RuneBook);
    WaitLag(WaitTime,WaitLagTime);
    if (GetGumpID(GetGumpsCount-1)<>RuneBookGumpID) then Continue;
    WaitGump(IntToStr(Destination*6+RecallMethod-6));
    WaitLag(WaitRecalTime,WaitLagTime);
  end;
  AddToSystemJournal('Среколились.');
end;

//==============================================================================
//# Check Metal
//==============================================================================

procedure CheckMetal;
var
Metal:Cardinal;
MetalCount,NeedMetalCount:Integer;
begin
  while True do begin
    CheckStates;
    NeedMetalCount:=IngotsAmount-(CountEx(IngotsType,IngotsColor,Backpack)+(CountEx(ShieldType,$0000,Backpack))*18);
    if NeedMetalCount<=0 then Exit;
    AddToSystemJournal('Проверяю металл.');
    AddToSystemJournal('Необходимо взять '+NeedMetalCount.ToString+' метала.');
    repeat
      if not Connected then Exit;
      UseObject(IngotsStorage);
      WaitLag(WaitTime,WaitLagTime);
    until(LastContainer = IngotsStorage);
    FindTypeEx(IngotsType,IngotsColor,IngotsStorage,False);
    Metal:=FindItem;
    MetalCount:=FindFullQuantity;
    AddToSystemJournal('Найдено метала '+MetalCount.ToString);
    if MetalCount<IngotsAmount then begin
      AddToSystemJournal('На жаль у Вас нет необходимого для продолжения работы скрипта необходимого количества металла. Скрипт остановлен');
    end;
    Grab(Metal,NeedMetalCount);
  end;
end;

//==============================================================================
//# CraftToolsMenu
//==============================================================================

procedure Create(NumF,NumS,Name:String);
begin
  WaitGump(NumF);
  WaitGump(NumS);
  AddToSystemJournal(Name);
  WaitLag(WaitTime*2,WaitLagTime);
end;

// =============================================================================
// Check Tools
// =============================================================================

function CheckTools(CheckType:Word;Count:Byte):Boolean;
begin
  CheckStates;
  CheckLag(WaitLagTime);
  Result:=CountEx(CheckType,0,Backpack)>Count-1;
end;

// =============================================================================
// Create Tools
// =============================================================================

procedure CreateTKTools;
begin
  if CountEx(TinkerType,0,Backpack)=0 then begin
    AddToSystemJournal('Не найдено ни одной тинкер тулзы. '+
    'Для нормальной работы скрипта в паке должна быть хоть одна тинкер тулза.'+
    'Скрипт остановлен');
    Halt;
  end;
  while not CheckTools(TinkerType,2) do begin
    CheckStates;
    UseType(TinkerType,0);
    Create(TKNumFirst,TKNumSecond,'Create tinker tools');
  end;
end;

function CreateCraftTools():Boolean;
begin
  if not CheckTools(TinkerType,2) then CreateTKTools;
  while not CheckTools(BlacksmithType,1) do begin
    CheckStates;
    UseType(TinkerType,0);
    Create(TKNumFirst,TKBSToolsNum,'Create blacksmith tools')
  end;
  Result:=True;
end;

// =============================================================================
// Create Shields
// =============================================================================

procedure CreateShields;
begin
  AddToSystemJournal('Начинаю щиты.');
  CloseGumps;
  while True do begin
    CheckStates;
    if CountEx(ShieldType,$0000,Backpack)>=ShieldAmount then begin
      CloseGumps;
      Break;
    end;
    if not CheckTools(BlacksmithType,1) then CreateCraftTools;
    UseType(BlacksmithType,0);
    Create(BSNumFirst,BSNumSecond,'Create shields');
    WaitLag(WaitTime,WaitLagTime);
    AddToSystemJournal('Сделано '+CountEx(ShieldType,$0000,Backpack).ToString+' щитов из '+ShieldAmount.ToString);
  end;
end;

// =============================================================================
// Sell Shields
// =============================================================================

procedure SellShields;
begin
  AddToSystemJournal('Продаём щиты.');
  AutoSell(ShieldType,$0000,CountEx(ShieldType,$0000,Backpack));
  while CountEx(ShieldType,$0000,Backpack)>0 do begin
    SetContextMenuHook(VendorID,VendorContextMenu);
    RequestContextMenu(VendorID);
    WaitLag(WaitRecalTime,WaitLagTime);
  end;
  AutoSell(ShieldType,$0000,0);
  AddToSystemJournal('Продали.');
end;

// =============================================================================
// Unload
// =============================================================================
procedure Unload;
var
BankBox:Cardinal;
Time:TDateTime;
begin
  repeat
    Time:=Now;
    UOSay('Bank');
    WaitLag(WaitTime,WaitLagTime);
  until(InJournalBetweenTimes('container has',Time,Now)>1);
  BankBox:=ObjAtLayer(BankLayer);
  while FindType(GoldType,Backpack)>1 do begin
    GoldTotal:=GoldTotal+FindFullQuantity;
    MoveItem(FindItem,FindFullQuantity,BankBox,0,0,0);
    WaitLag(WaitTime,WaitLagTime);
  end;
  AddToSystemJournal('TotalGold = '+GoldTotal.ToString);
end;

begin
  if not GetARStatus then SetARStatus(True);
  SetAutoSellDelay(3);
  CheckStates;
  Initial;
  while True do begin
    RecallTo(HomeRune,HomeX,HomeY,'Домой');
    CheckMetal;
    RecallTo(ForgeRune,ForgeX,ForgeY,'Кузня');
    CreateShields;
    RecallTo(VendorRune,VendorX,VendorY,'Вендор');
    SellShields;
    RecallTo(BankRune,BankX,BankY,'Bank');
    Unload;
  end;
end.
