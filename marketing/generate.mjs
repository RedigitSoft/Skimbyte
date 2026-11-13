#!/usr/bin/env node
// Regenerates every marketing asset in marketing/out/ from the HTML templates in
// marketing/templates/ and the raw captures in marketing/shots/.
//
// One-time setup:   npm i --prefix marketing playwright-core
// Also needs:       rsvg-convert (brew install librsvg) and macOS `sips` for resizing.
// Run:              node marketing/generate.mjs
//
// Capturing fresh app screenshots is a separate, interactive step — see marketing/README.md.

import { createRequire } from 'node:module';
import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, copyFileSync, readdirSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { homedir } from 'node:os';

const ROOT = dirname(fileURLToPath(import.meta.url));           // marketing/
const REPO = resolve(ROOT, '..');
const T = join(ROOT, 'templates');
const OUT = join(ROOT, 'out');

const require = createRequire(join(ROOT, 'node_modules', 'playwright-core', 'package.json'));
let chromium;
try { ({ chromium } = require('playwright-core')); }
catch { console.error('playwright-core missing — run: npm i --prefix marketing playwright-core'); process.exit(1); }

function findChromium() {
  if (process.env.CHROME_PATH && existsSync(process.env.CHROME_PATH)) return process.env.CHROME_PATH;
  const cache = join(homedir(), 'Library/Caches/ms-playwright');
  if (existsSync(cache)) {
    for (const dir of readdirSync(cache).filter(d => d.startsWith('chromium-')).sort().reverse()) {
      for (const sub of ['chrome-mac-arm64', 'chrome-mac']) {
        for (const app of ['Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing',
                           'Chromium.app/Contents/MacOS/Chromium']) {
          const p = join(cache, dir, sub, app);
          if (existsSync(p)) return p;
        }
      }
    }
  }
  const sys = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
  if (existsSync(sys)) return sys;
  throw new Error('No Chromium found. Set CHROME_PATH or run: npx playwright install chromium');
}

// ---------- what gets rendered ----------

// The five store panels. Captions are baked into the images; edit here and re-run.
const PANELS = [
  { out: '01-feed',       img: 'feed-dark.png',        theme: 'dark',  accent: '7C5CFC',
    title: "The day's dev news, <em>skimmed</em>.", sub: 'Short, readable briefs from the sources that matter.' },
  { out: '02-topics',     img: 'onboarding-light.png', theme: 'light', accent: '7C5CFC',
    title: 'Pick your <em>topics</em>.',            sub: 'Kotlin to XR — follow what you build.' },
  { out: '03-feed-light', img: 'feed-light.png',       theme: 'light', accent: 'EB5E28',
    title: 'One brief at a time.',                  sub: "Finish the day's news with your coffee." },
  { out: '04-appearance', img: 'appearance-light.png', theme: 'light', accent: 'EB5E28',
    title: 'Make it <em>yours</em>.',               sub: 'Matte black or off-white, and ten sharp accents.' },
  { out: '05-detail',     img: 'detail-dark.png',      theme: 'dark',  accent: '7C5CFC',
    title: 'Read the gist in a minute.',            sub: "Jump to the full source when it's worth it." },
];

// Flat renders: template → viewport → output (master is @dpr, final via sips where set).
const FLATS = [
  { tpl: 'feature-graphic.html',  w: 1024, h: 500, dpr: 2, master: 'play/feature-graphic@2x.png',       final: ['play/feature-graphic.png', 1024, 500] },
  { tpl: 'social-banner.html',    w: 1200, h: 630, dpr: 2, master: 'social/banner@2x.png',              final: ['social/banner-1200x630.png', 1200, 630] },
  { tpl: 'github-preview.html',   w: 1280, h: 640, dpr: 2, master: 'social/github-preview@2x.png',      final: ['social/github-preview-1280x640.png', 1280, 640] },
  { tpl: 'desktop-hero.html',     w: 1440, h: 810, dpr: 2, master: 'desktop/hero-2880x1620.png' },
  { tpl: 'post-placeholder.html?theme=light', w: 1600, h: 1000, dpr: 1, master: 'placeholder/post-placeholder-light.png' },
  { tpl: 'post-placeholder.html?theme=dark',  w: 1600, h: 1000, dpr: 1, master: 'placeholder/post-placeholder-dark.png' },
];

// ---------- pipeline ----------

const sips = (inFile, w, h, outFile) =>
  execFileSync('sips', ['-z', String(h), String(w), inFile, '--out', outFile], { stdio: 'pipe' });

const url = (rel) => {
  const [file, query] = rel.split('?');
  return pathToFileURL(join(T, file)).href + (query ? '?' + query : '');
};

const browser = await chromium.launch({ headless: true, executablePath: findChromium() });
async function shot(pageUrl, w, h, dpr, outFile) {
  const ctx = await browser.newContext({ viewport: { width: w, height: h }, deviceScaleFactor: dpr });
  const page = await ctx.newPage();
  await page.goto(pageUrl, { waitUntil: 'networkidle' });
  await page.waitForTimeout(700);
  mkdirSync(dirname(outFile), { recursive: true });
  await page.screenshot({ path: outFile, type: 'png' });
  await ctx.close();
  console.log('  rendered', outFile.replace(REPO + '/', ''));
}

// Store panels: App Store masters at 1290×2796, Play derived at 1080×2340.
for (const p of PANELS) {
  const q = new URLSearchParams({ img: p.img, theme: p.theme, accent: p.accent, title: p.title, sub: p.sub });
  const master = join(OUT, 'appstore', p.out + '.png');
  await shot(url('panel.html') + '?' + q, 430, 932, 3, master);
  mkdirSync(join(OUT, 'play'), { recursive: true });
  sips(master, 1080, 2340, join(OUT, 'play', p.out + '.png'));
}

// Banners, feature graphic, hero, placeholders.
for (const f of FLATS) {
  const master = join(OUT, f.master);
  await shot(url(f.tpl), f.w, f.h, f.dpr, master);
  if (f.final) {
    const [rel, w, h] = f.final;
    sips(master, w, h, join(OUT, rel));
  }
}
await browser.close();

// Icons: stores want full squares (they apply their own masks), so flatten the corner radius.
const squareSvg = join(OUT, '.icon-square.svg');
execFileSync('sh', ['-c',
  `sed 's/rx="112"/rx="0"/' "${join(REPO, 'website', 'app-icon.svg')}" > "${squareSvg}"`]);
execFileSync('rsvg-convert', ['-w', '512', '-h', '512', squareSvg, '-o', join(OUT, 'play', 'icon-512.png')]);
execFileSync('rsvg-convert', ['-w', '1024', '-h', '1024', squareSvg, '-o', join(OUT, 'appstore', 'icon-1024.png')]);
console.log('  rendered icons');

// Demo mode loads these from the website (see DemoNetworkModule's imageUrl constants).
for (const theme of ['light', 'dark']) {
  const src = join(OUT, 'placeholder', `post-placeholder-${theme}.png`);
  const dst = join(REPO, 'website', `post-placeholder-${theme}.png`);
  sips(src, 1200, 750, dst);
  console.log('  copied', dst.replace(REPO + '/', ''));
}

// The social banner doubles as the website's Open Graph image.
copyFileSync(join(OUT, 'social', 'banner-1200x630.png'), join(REPO, 'website', 'og-image.png'));
console.log('  copied website/og-image.png');
console.log('done.');
