# 🌙 Dead Sleep

**Sleep on time. Wake up on time.**

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![OOP](https://img.shields.io/badge/Object--Oriented-Design-8C7CFB?style=flat-square)
![Status](https://img.shields.io/badge/Status-Student%20Project-blue?style=flat-square)

<p align="center">
  <img src="assets/demo.gif" alt="Dead Sleep app demo" width="700">
</p>

<!--
  Replace assets/demo.gif with the exported clip from the app demo.
  Create an /assets folder in your repo root and drop the GIF/MP4 there,
  or update the path above to wherever you host it.
-->

## About

Dead Sleep is a Java-based Android project made to help users sleep on time, wake up on time, and reduce unnecessary phone distractions during their sleeping hours.

The idea is simple: the user sets a sleep schedule, chooses distracting apps, and the app helps them stay away from those apps during that time.

## Main Features

- Set a personal sleep and wake-up schedule.
- Get a reminder before sleep time.
- Automatically start sleep mode at the scheduled time.
- Keep selected distracting apps restricted during sleep time.
- The Dead Sleep app cannot be deleted during the scheduled sleep time.
- Show a smart alarm at wake-up time.
- Stop the alarm only after completing a puzzle, math problem, game, or other challenge.
- Allow users to customize their alarm settings.

## Main Modules

| Module | Responsibility |
|---|---|
| **User Setup** | First-time onboarding and preference collection |
| **Sleep Schedule** | Sleep/wake time selection and weekly repeat rules |
| **App Restriction** | Selecting and locking distracting apps during sleep |
| **Reminder System** | Bedtime notifications ahead of scheduled sleep |
| **Smart Alarm** | Wake-up alarm with sound, volume, and snooze settings |
| **Challenge System** | Puzzle / math / game challenge required to stop the alarm |
| **Settings** | App-wide configuration and personalization |

## Tech Stack

- **Language:** Java
- **Platform:** Android
- **Architecture:** Object-Oriented Programming (OOP)

## Future Improvements

- Add more types of alarm challenges.
- Add weekly sleep statistics and progress.
- Give better routine suggestions based on user habits.
- Add more flexible app restriction options.
- Improve the UI and personalization.
- Add more smart reminder options.

## Team

Team Project — built as part of an Android application development.



<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>Dead Sleep — App Demo</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@500;600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
<style>
  :root{
    --bg-0:#0A0E1F;
    --bg-1:#111731;
    --surface:#1A2140;
    --surface-2:#212A4E;
    --stroke:rgba(255,255,255,0.08);
    --violet:#8C7CFB;
    --blue:#5B8CFF;
    --gold:#F4C77B;
    --rose:#FF7A85;
    --text-hi:#F2F3FC;
    --text-lo:#8D93B8;
    --text-faint:#5A6088;
    box-sizing:border-box;
    padding-top:env(safe-area-inset-top,0px);
    padding-bottom:env(safe-area-inset-bottom,0px);
  }
  *{box-sizing:border-box;}
  html{scroll-padding-top:env(safe-area-inset-top,0px);}
  html,body{height:100%;margin:0;}
  body{
    background:
      radial-gradient(60% 50% at 18% 12%, rgba(140,124,251,0.16), transparent 60%),
      radial-gradient(55% 45% at 85% 85%, rgba(91,140,255,0.14), transparent 60%),
      var(--bg-0);
    color:var(--text-hi);
    font-family:'Plus Jakarta Sans', system-ui, sans-serif;
    display:flex;
    align-items:center;
    justify-content:center;
    overflow:hidden;
    position:relative;
  }

  /* ---------- floating particles ---------- */
  .particles{position:fixed;inset:0;pointer-events:none;overflow:hidden;}
  .particle{
    position:absolute;border-radius:50%;
    background:radial-gradient(circle, rgba(140,124,251,0.55), transparent 70%);
    filter:blur(1px);
    animation:drift 14s ease-in-out infinite;
  }
  @keyframes drift{
    0%,100%{transform:translateY(0) translateX(0);opacity:.35;}
    50%{transform:translateY(-22px) translateX(10px);opacity:.75;}
  }

  /* ---------- overall composition ---------- */
  .stage{
    position:relative;
    width:min(92vw, 1180px);
    height:min(88vh, 700px);
    display:flex;
    align-items:center;
    justify-content:center;
    gap:64px;
  }

  .sidecopy{
    width:300px;
    flex:0 0 300px;
    display:flex;
    flex-direction:column;
    gap:18px;
  }
  .brandmark{
    display:flex;align-items:center;gap:10px;
    color:var(--text-lo);font-size:13px;letter-spacing:.02em;
  }
  .brandmark .dot{width:7px;height:7px;border-radius:50%;background:var(--gold);box-shadow:0 0 10px var(--gold);}
  .headline{
    font-family:'Space Grotesk', sans-serif;
    font-size:clamp(26px,3vw,34px);
    font-weight:600;
    line-height:1.18;
    letter-spacing:-0.01em;
  }
  .headline .accent{color:var(--violet);}
  .subline{
    color:var(--text-lo);
    font-size:15px;
    line-height:1.6;
    max-width:30ch;
    min-height:70px;
  }
  .scenetrack{
    display:flex;
    flex-wrap:wrap;
    gap:7px;
    max-width:300px;
  }
  .scenetrack span{
    width:20px;height:4px;border-radius:3px;
    background:var(--surface-2);
    transition:background .4s ease, width .4s ease;
  }
  .scenetrack span.on{background:linear-gradient(90deg,var(--violet),var(--blue));width:30px;}

  .techrow{display:flex;gap:8px;margin-top:auto;}
  .techrow span{
    font-size:11px;color:var(--text-faint);
    border:1px solid var(--stroke);
    padding:5px 10px;border-radius:20px;
    letter-spacing:.02em;
  }

  /* ---------- phone ---------- */
  .phonewrap{
    position:relative;
    flex:0 0 auto;
    filter:drop-shadow(0 30px 60px rgba(0,0,0,0.55));
  }
  .phone{
    width:284px;height:580px;
    background:linear-gradient(180deg,#161C38,#0D1128);
    border-radius:42px;
    padding:12px;
    position:relative;
    border:1px solid rgba(255,255,255,0.08);
  }
  .notch{
    position:absolute;top:12px;left:50%;transform:translateX(-50%);
    width:90px;height:20px;background:#0A0E1F;border-radius:12px;
    z-index:5;
  }
  .screen{
    position:relative;
    width:100%;height:100%;
    background:linear-gradient(165deg,var(--bg-1),#0B0F26 70%);
    border-radius:32px;
    overflow:hidden;
  }
  .statusbar{
    display:flex;justify-content:space-between;align-items:center;
    padding:16px 20px 4px;
    font-size:12px;font-weight:600;color:var(--text-hi);
    position:relative;z-index:4;
  }
  .statusbar .icons{display:flex;gap:5px;color:var(--text-lo);font-size:10px;}

  .scene{
    position:absolute;inset:0;
    padding:38px 18px 20px;
    opacity:0;
    transform:translateY(14px) scale(.985);
    pointer-events:none;
    transition:opacity .55s ease, transform .55s ease;
  }
  .scene.active{
    opacity:1;
    transform:translateY(0) scale(1);
    pointer-events:auto;
  }

  .card{
    background:var(--surface);
    border:1px solid var(--stroke);
    border-radius:16px;
    padding:14px 16px;
  }
  .rowline{display:flex;align-items:center;justify-content:space-between;}
  .pill{
    display:inline-flex;align-items:center;gap:6px;
    background:var(--surface-2);
    border-radius:20px;padding:6px 12px;font-size:11px;color:var(--text-lo);
  }
  .btn{
    display:inline-flex;align-items:center;justify-content:center;
    background:linear-gradient(135deg,var(--violet),var(--blue));
    color:#fff;font-weight:700;font-size:12.5px;
    border-radius:12px;padding:11px 14px;
    box-shadow:0 8px 20px rgba(91,140,255,0.28);
  }
  .btn.ghost{
    background:var(--surface-2);color:var(--text-hi);box-shadow:none;
    border:1px solid var(--stroke);
  }

  /* ---- Scene 1: Intro ---- */
  #s1{display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;gap:14px;}
  .moonlock{
    width:76px;height:76px;border-radius:24px;
    background:linear-gradient(150deg,var(--surface-2),var(--surface));
    border:1px solid var(--stroke);
    display:flex;align-items:center;justify-content:center;
    box-shadow:0 0 0 8px rgba(140,124,251,0.06);
  }
  .moonlock svg{width:38px;height:38px;}
  #s1 h1{font-family:'Space Grotesk';font-size:24px;letter-spacing:.02em;margin:0;}
  #s1 p{color:var(--text-lo);font-size:13px;margin:0;}

  /* ---- Scene 2: Onboarding ---- */
  .qcard{margin-bottom:10px;}
  .qcard .q{font-size:12.5px;color:var(--text-lo);margin-bottom:9px;}
  .optrow{display:flex;gap:8px;}
  .opt{
    flex:1;text-align:center;padding:8px 6px;border-radius:10px;
    background:var(--surface-2);font-size:11.5px;color:var(--text-lo);
    border:1px solid var(--stroke);
  }
  .opt.picked{
    background:linear-gradient(135deg,rgba(140,124,251,.25),rgba(91,140,255,.2));
    color:var(--text-hi);border-color:var(--violet);
  }

  /* ---- Scene 3: Dashboard ---- */
  .bigstat{display:flex;justify-content:space-between;margin:14px 0;}
  .bigstat .num{font-family:'Space Grotesk';font-size:26px;font-weight:600;}
  .bigstat .lab{font-size:10.5px;color:var(--text-lo);margin-top:2px;}
  .listcard{display:flex;flex-direction:column;gap:9px;margin-top:12px;}
  .listrow{display:flex;justify-content:space-between;align-items:center;font-size:12px;}
  .listrow .k{color:var(--text-lo);}
  .listrow .v{font-weight:700;}
  .status-ok{color:var(--gold);}

  /* ---- Scene 4: schedule ---- */
  .timeselect{display:flex;gap:10px;margin:12px 0;}
  .timeselect .tcard{flex:1;text-align:center;}
  .timeselect .tval{font-family:'Space Grotesk';font-size:20px;font-weight:600;margin-top:4px;}
  .weekrow{display:flex;justify-content:space-between;margin-top:14px;}
  .day{
    width:28px;height:28px;border-radius:50%;
    display:flex;align-items:center;justify-content:center;
    font-size:10px;background:var(--surface-2);color:var(--text-lo);
  }
  .day.on{background:linear-gradient(135deg,var(--violet),var(--blue));color:#fff;}

  /* ---- Scene 5: app select ---- */
  .applist{display:flex;flex-direction:column;gap:9px;margin-top:10px;}
  .approw{display:flex;align-items:center;gap:10px;padding:9px 10px;}
  .appicon{width:30px;height:30px;border-radius:9px;flex:0 0 auto;}
  .approw .name{font-size:12.5px;flex:1;}
  .check{width:18px;height:18px;border-radius:6px;border:1.5px solid var(--text-faint);flex:0 0 auto;}
  .check.on{background:var(--violet);border-color:var(--violet);position:relative;}
  .check.on::after{content:'✓';position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:11px;color:#fff;}

  /* ---- Scene 6: notification ---- */
  #s6{padding-top:70px;}
  .notif{
    background:var(--surface);border:1px solid var(--stroke);
    border-radius:16px;padding:12px 14px;
    display:flex;gap:10px;
    transform:translateY(-30px);opacity:0;
    animation:none;
  }
  .active .notif{animation:slideNotif .7s .25s ease forwards;}
  @keyframes slideNotif{to{transform:translateY(0);opacity:1;}}
  .notif .ic{width:32px;height:32px;border-radius:9px;background:linear-gradient(135deg,var(--gold),#e0a94f);flex:0 0 auto;display:flex;align-items:center;justify-content:center;}
  .notif .t1{font-size:12px;font-weight:700;}
  .notif .t2{font-size:11.5px;color:var(--text-lo);margin-top:2px;line-height:1.4;}

  /* ---- Scene 7: sleep mode active ---- */
  #s7{display:flex;flex-direction:column;align-items:center;text-align:center;gap:10px;padding-top:26px;}
  .glowring{
    width:64px;height:64px;border-radius:50%;
    background:radial-gradient(circle, rgba(140,124,251,.35), transparent 70%);
    display:flex;align-items:center;justify-content:center;
    border:1px solid rgba(140,124,251,.5);
  }
  .miniapps{display:flex;gap:10px;margin-top:6px;}
  .miniapp{position:relative;width:38px;height:38px;border-radius:11px;}
  .miniapp .lk{
    position:absolute;inset:0;border-radius:11px;
    background:rgba(10,12,26,0.72);
    display:flex;align-items:center;justify-content:center;font-size:13px;
    opacity:0;transition:opacity .4s ease .3s;
  }
  .active .miniapp .lk{opacity:1;}

  /* ---- Scene 8: delete protection ---- */
  #s8{display:flex;flex-direction:column;align-items:center;text-align:center;gap:12px;padding-top:34px;}
  .trashrow{display:flex;align-items:center;gap:8px;color:var(--text-lo);font-size:12px;}
  .blockbadge{
    display:flex;align-items:center;gap:8px;
    background:rgba(255,122,133,0.12);border:1px solid rgba(255,122,133,0.4);
    color:var(--rose);padding:9px 14px;border-radius:12px;font-size:12px;font-weight:700;
  }

  /* ---- Scene 9: blocked app ---- */
  #s9{display:flex;flex-direction:column;align-items:center;text-align:center;gap:10px;padding-top:40px;}
  .lockcircle{
    width:56px;height:56px;border-radius:50%;
    background:var(--surface-2);border:1px solid var(--stroke);
    display:flex;align-items:center;justify-content:center;
  }
  .rangepill{
    margin-top:10px;font-family:'Space Grotesk';font-size:14px;color:var(--text-lo);
  }

  /* ---- Scene 10: alarm ---- */
  #s10{display:flex;flex-direction:column;align-items:center;text-align:center;gap:6px;padding-top:30px;}
  #s10 .greet{font-size:12px;color:var(--text-lo);letter-spacing:.03em;}
  #s10 .time{font-family:'Space Grotesk';font-size:44px;font-weight:700;margin:2px 0 10px;}
  .waves{display:flex;gap:4px;align-items:flex-end;height:26px;}
  .waves i{
    width:4px;border-radius:3px;background:linear-gradient(180deg,var(--gold),var(--violet));
    animation:wave 1s ease-in-out infinite;
  }
  .waves i:nth-child(1){height:10px;animation-delay:0s;}
  .waves i:nth-child(2){height:22px;animation-delay:.1s;}
  .waves i:nth-child(3){height:14px;animation-delay:.2s;}
  .waves i:nth-child(4){height:26px;animation-delay:.3s;}
  .waves i:nth-child(5){height:12px;animation-delay:.4s;}
  .waves i:nth-child(6){height:20px;animation-delay:.5s;}
  .waves i:nth-child(7){height:9px;animation-delay:.6s;}
  @keyframes wave{0%,100%{transform:scaleY(.5);}50%{transform:scaleY(1);}}
  .alarmopts{display:flex;flex-wrap:wrap;gap:7px;justify-content:center;margin-top:14px;}

  /* ---- Scene 11: challenge ---- */
  #s11{display:flex;flex-direction:column;align-items:center;text-align:center;gap:12px;padding-top:34px;}
  .puzzle{
    font-family:'Space Grotesk';font-size:26px;font-weight:600;
    letter-spacing:.02em;
  }
  .answerbox{
    width:70px;height:40px;border-radius:10px;background:var(--surface-2);
    border:1px solid var(--stroke);
    display:flex;align-items:center;justify-content:center;
    font-family:'Space Grotesk';font-size:17px;
  }
  .answerbox span{opacity:0;}
  .active .answerbox span{animation:typein .3s .9s forwards;}
  @keyframes typein{to{opacity:1;}}
  .successtag{
    display:flex;align-items:center;gap:6px;color:var(--gold);font-size:12.5px;font-weight:700;
    opacity:0;transform:translateY(6px);
  }
  .active .successtag{animation:popsuccess .5s 1.5s ease forwards;}
  @keyframes popsuccess{to{opacity:1;transform:translateY(0);}}

  /* ---- Scene 12: outro ---- */
  #s12{display:flex;flex-direction:column;gap:12px;padding-top:30px;}
  .comingsoon-grid{display:grid;grid-template-columns:1fr 1fr;gap:8px;}
  .cs-card{padding:11px;display:flex;flex-direction:column;gap:6px;}
  .cs-card .ic{font-size:15px;}
  .cs-card .lab{font-size:10.5px;color:var(--text-lo);line-height:1.3;}
  .badgesrow{display:flex;gap:6px;justify-content:center;margin-top:4px;}
  .badgesrow .b{font-size:10px;color:var(--text-lo);border:1px solid var(--stroke);border-radius:16px;padding:4px 9px;}
  .teamline{text-align:center;font-size:11px;color:var(--text-faint);margin-top:6px;}

  @media (prefers-color-scheme: light){
    :root:not([data-theme="light"]){ }
  }

  @media (max-width: 860px){
    .stage{flex-direction:column;height:auto;gap:26px;padding:24px 0;}
    .sidecopy{width:min(90vw,320px);flex:none;align-items:center;text-align:center;}
    .subline{max-width:34ch;}
    .techrow{margin-top:6px;}
  }
</style>
</head>
<body>

<div class="particles" id="particles"></div>

<div class="stage">
  <div class="sidecopy">
    <div class="brandmark"><span class="dot"></span> DEAD SLEEP · Android Demo</div>
    <div class="headline" id="headline">Sleep on time. <span class="accent">Wake up on time.</span></div>
    <div class="subline" id="subline">A student Android project that builds a real bedtime routine — scheduling, app blocking, and a smart alarm you can't sleep through.</div>
    <div class="scenetrack" id="track"></div>
    <div class="techrow"><span>Java</span><span>Android SDK</span><span>OOP</span></div>
  </div>

  <div class="phonewrap">
    <div class="phone">
      <div class="notch"></div>
      <div class="screen">
        <div class="statusbar"><span id="clock">9:42 PM</span><span class="icons">●●●&nbsp;LTE&nbsp;▮▮▮</span></div>

        <!-- Scene 1: Intro -->
        <div class="scene" id="s1">
          <div class="moonlock">
            <svg viewBox="0 0 24 24" fill="none"><path d="M20 14.5A8.5 8.5 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z" fill="url(#g1)"/><defs><linearGradient id="g1" x1="0" y1="0" x2="24" y2="24"><stop stop-color="#8C7CFB"/><stop offset="1" stop-color="#5B8CFF"/></linearGradient></defs></svg>
          </div>
          <h1>DEAD SLEEP</h1>
          <p>Sleep on time. Wake up on time.</p>
        </div>

        <!-- Scene 2: Onboarding -->
        <div class="scene" id="s2">
          <div class="rowline" style="margin-bottom:14px;">
            <span style="font-size:12.5px;font-weight:700;">Quick setup</span>
            <span class="pill">2 / 6</span>
          </div>
          <div class="qcard">
            <div class="q">What time do you usually sleep?</div>
            <div class="optrow">
              <div class="opt">10 PM</div><div class="opt picked">11 PM</div><div class="opt">12 AM</div>
            </div>
          </div>
          <div class="qcard">
            <div class="q">Which apps distract you before bed?</div>
            <div class="optrow">
              <div class="opt picked">Social</div><div class="opt">Video</div><div class="opt picked">Games</div>
            </div>
          </div>
          <div class="qcard">
            <div class="q">Want a smart wake-up challenge?</div>
            <div class="optrow"><div class="opt picked">Yes, math puzzle</div></div>
          </div>
        </div>

        <!-- Scene 3: Dashboard -->
        <div class="scene" id="s3">
          <div style="font-size:12.5px;color:var(--text-lo);">Tonight's routine</div>
          <div class="bigstat">
            <div><div class="num">11:00<span style="font-size:12px;">PM</span></div><div class="lab">Sleep</div></div>
            <div><div class="num">7:00<span style="font-size:12px;">AM</span></div><div class="lab">Wake</div></div>
          </div>
          <div class="card listcard">
            <div class="listrow"><span class="k">Sleep mode</span><span class="v status-ok">Scheduled</span></div>
            <div class="listrow"><span class="k">Next reminder</span><span class="v">10:30 PM</span></div>
            <div class="listrow"><span class="k">Blocked apps</span><span class="v">3 selected</span></div>
          </div>
          <div class="optrow" style="margin-top:12px;">
            <div class="btn ghost" style="flex:1;">Edit Schedule</div>
            <div class="btn ghost" style="flex:1;">Blocked Apps</div>
          </div>
        </div>

        <!-- Scene 4: Schedule -->
        <div class="scene" id="s4">
          <div style="font-size:12.5px;font-weight:700;">Sleep schedule</div>
          <div class="timeselect">
            <div class="card tcard"><div class="lab" style="color:var(--text-lo);font-size:10px;">SLEEP</div><div class="tval">11:00 PM</div></div>
            <div class="card tcard"><div class="lab" style="color:var(--text-lo);font-size:10px;">WAKE</div><div class="tval">7:00 AM</div></div>
          </div>
          <div class="card" style="padding:14px;">
            <div class="rowline"><span style="font-size:12px;">Repeat weekly</span><span class="pill" style="background:var(--violet);color:#fff;">On</span></div>
            <div class="weekrow">
              <div class="day on">S</div><div class="day on">S</div><div class="day on">M</div><div class="day on">T</div><div class="day on">W</div><div class="day on">T</div><div class="day">F</div>
            </div>
          </div>
        </div>

        <!-- Scene 5: App selection -->
        <div class="scene" id="s5">
          <div class="rowline"><span style="font-size:12.5px;font-weight:700;">Select apps to block</span><span class="pill">3 selected</span></div>
          <div class="applist">
            <div class="card approw"><div class="appicon" style="background:linear-gradient(135deg,#8C7CFB,#5B8CFF);"></div><span class="name">Social Media</span><div class="check on"></div></div>
            <div class="card approw"><div class="appicon" style="background:linear-gradient(135deg,#F4C77B,#e0a94f);"></div><span class="name">Video</span><div class="check on"></div></div>
            <div class="card approw"><div class="appicon" style="background:linear-gradient(135deg,#5B8CFF,#8C7CFB);"></div><span class="name">Games</span><div class="check on"></div></div>
            <div class="card approw"><div class="appicon" style="background:var(--surface-2);"></div><span class="name">Browser</span><div class="check"></div></div>
          </div>
          <div class="card" style="margin-top:10px;padding:10px 12px;font-size:11px;color:var(--text-lo);">Apps will be restricted during sleep time.</div>
        </div>

        <!-- Scene 6: Bedtime reminder -->
        <div class="scene" id="s6">
          <div class="notif">
            <div class="ic">🌙</div>
            <div>
              <div class="t1">Bedtime Reminder</div>
              <div class="t2">Your sleep time starts in 30 minutes.<br>Time to finish your screen time.</div>
            </div>
          </div>
        </div>

        <!-- Scene 7: Sleep mode active -->
        <div class="scene" id="s7">
          <div class="glowring">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none"><path d="M20 14.5A8.5 8.5 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z" fill="#8C7CFB"/></svg>
          </div>
          <div style="font-family:'Space Grotesk';font-size:16px;font-weight:600;">SLEEP MODE ACTIVE</div>
          <div style="font-size:11px;color:var(--text-lo);">Selected apps are now restricted</div>
          <div class="miniapps">
            <div class="miniapp" style="background:linear-gradient(135deg,#8C7CFB,#5B8CFF);"><div class="lk">🔒</div></div>
            <div class="miniapp" style="background:linear-gradient(135deg,#F4C77B,#e0a94f);"><div class="lk">🔒</div></div>
            <div class="miniapp" style="background:linear-gradient(135deg,#5B8CFF,#8C7CFB);"><div class="lk">🔒</div></div>
          </div>
        </div>

        <!-- Scene 8: delete protection -->
        <div class="scene" id="s8">
          <div class="trashrow"><span>🗑️</span><span>Uninstall Dead Sleep</span></div>
          <div class="blockbadge">🔒 Sleep Protection Active</div>
          <div style="font-size:11.5px;color:var(--text-lo);max-width:190px;">Dead Sleep can't be deleted during scheduled sleep time.</div>
        </div>

        <!-- Scene 9: Blocked app access -->
        <div class="scene" id="s9">
          <div class="lockcircle">🔒</div>
          <div style="font-family:'Space Grotesk';font-size:15px;font-weight:600;">App Locked</div>
          <div style="font-size:11.5px;color:var(--text-lo);">Sleep time is active.</div>
          <div class="rangepill">11:00 PM — 7:00 AM</div>
        </div>

        <!-- Scene 10: Alarm -->
        <div class="scene" id="s10">
          <div class="greet">GOOD MORNING</div>
          <div class="time">7:00</div>
          <div class="waves"><i></i><i></i><i></i><i></i><i></i><i></i><i></i></div>
          <div class="alarmopts">
            <span class="pill">Sound: Chime</span>
            <span class="pill">Volume: High</span>
            <span class="pill">Snooze: Off</span>
          </div>
        </div>

        <!-- Scene 11: Challenge -->
        <div class="scene" id="s11">
          <div style="font-size:11.5px;color:var(--text-lo);">Solve to stop the alarm</div>
          <div class="puzzle">17 × 8 = ?</div>
          <div class="answerbox"><span>136</span></div>
          <div class="successtag">✓ Challenge Completed — Alarm Stopped</div>
        </div>

        <!-- Scene 12: Outro -->
        <div class="scene" id="s12">
          <div style="text-align:center;font-size:12px;font-weight:700;">Coming soon</div>
          <div class="comingsoon-grid">
            <div class="card cs-card"><span class="ic">📊</span><span class="lab">Sleep statistics</span></div>
            <div class="card cs-card"><span class="ic">📈</span><span class="lab">Weekly progress</span></div>
            <div class="card cs-card"><span class="ic">🧩</span><span class="lab">More challenges</span></div>
            <div class="card cs-card"><span class="ic">✨</span><span class="lab">Smarter routines</span></div>
          </div>
          <div class="badgesrow"><span class="b">Java</span><span class="b">Android</span><span class="b">OOP</span></div>
          <div class="teamline">Student Team Project</div>
        </div>

      </div>
    </div>
  </div>
</div>

<script>
  // floating particles
  const pWrap = document.getElementById('particles');
  for(let i=0;i<16;i++){
    const p = document.createElement('div');
    p.className='particle';
    const size = 3+Math.random()*5;
    p.style.width=size+'px';p.style.height=size+'px';
    p.style.left=Math.random()*100+'vw';
    p.style.top=Math.random()*100+'vh';
    p.style.animationDuration=(10+Math.random()*10)+'s';
    p.style.animationDelay=(Math.random()*6)+'s';
    pWrap.appendChild(p);
  }

  const scenes = [
    {id:'s1', clock:'9:42 PM', head:'Sleep on time. <span class="accent">Wake up on time.</span>', sub:"A student Android project that builds a real bedtime routine — scheduling, app blocking, and a smart alarm you can't sleep through."},
    {id:'s2', clock:'9:45 PM', head:'A routine built <span class="accent">around you</span>', sub:'First launch walks through a short setup: sleep time, distracting apps, and how you want to wake up.'},
    {id:'s3', clock:'10:15 PM', head:'One dashboard, <span class="accent">every setting</span>', sub:'Sleep time, wake time, blocked apps and reminders — all visible at a glance.'},
    {id:'s4', clock:'10:16 PM', head:'Set your <span class="accent">sleep window</span>', sub:'Pick sleep and wake times, then choose which days the routine repeats.'},
    {id:'s5', clock:'10:20 PM', head:'Choose what to <span class="accent">block</span>', sub:'Social, video and games can be restricted automatically once sleep mode begins.'},
    {id:'s6', clock:'10:30 PM', head:'A nudge <span class="accent">before bed</span>', sub:'A bedtime reminder arrives 30 minutes ahead, so wind-down isn\'t a surprise.'},
    {id:'s7', clock:'11:00 PM', head:'Sleep mode <span class="accent">takes over</span>', sub:'At 11:00 PM, selected apps lock automatically — no manual step required.'},
    {id:'s8', clock:'11:05 PM', head:"Can't uninstall <span class=\"accent\">your way out</span>", sub:"Dead Sleep protects itself from being removed during scheduled sleep hours."},
    {id:'s9', clock:'11:10 PM', head:'Blocked apps <span class="accent">stay blocked</span>', sub:'Opening a restricted app shows the active sleep window instead.'},
    {id:'s10', clock:'7:00 AM', head:'An alarm you'+'\u2019'+'ll actually <span class="accent">hear</span>', sub:'Customizable sound, volume and snooze — built to be hard to ignore.'},
    {id:'s11', clock:'7:00 AM', head:'Solve it to <span class="accent">silence it</span>', sub:'A quick math, memory or reaction challenge replaces the ordinary stop button.'},
    {id:'s12', clock:'7:02 AM', head:'Built with <span class="accent">Java + OOP</span>', sub:"A student Android project, with statistics, more challenges and smarter routines on the way."},
  ];

  const phone = document.querySelector('.screen');
  const clockEl = document.getElementById('clock');
  const headEl = document.getElementById('headline');
  const subEl = document.getElementById('subline');
  const track = document.getElementById('track');

  scenes.forEach((s,i)=>{
    const dot = document.createElement('span');
    dot.dataset.i = i;
    track.appendChild(dot);
  });

  let idx = 0;
  function render(){
    scenes.forEach((s,i)=>{
      document.getElementById(s.id).classList.toggle('active', i===idx);
      track.children[i].classList.toggle('on', i===idx);
    });
    const s = scenes[idx];
    clockEl.textContent = s.clock;
    headEl.innerHTML = s.head;
    subEl.textContent = subEl.textContent; // no-op guard
    subEl.innerHTML = s.sub;
    idx = (idx+1) % scenes.length;
  }
  render();
  setInterval(render, 2700);
</script>

</body>
</html>

