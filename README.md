# Water Fill Challenge

Static web preview of the water-fill game. The playable page lives at `web/index.html` and requires no build step or dependencies.

## Deploy to Vercel

### Option 1: Vercel dashboard

1. Push this folder to a GitHub repository.
2. Import the repository at [vercel.com/new](https://vercel.com/new).
3. Keep the framework preset as **Other**.
4. Leave the build command and output directory empty.
5. Click **Deploy**.

The included `vercel.json` routes the deployed root URL to the game preview.

### Option 2: Vercel CLI

```powershell
cd C:\Users\aarah\water-fill-game
npm i -g vercel
vercel
```

For a production deployment:

```powershell
vercel --prod
```
