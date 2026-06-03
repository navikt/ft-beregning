# ft-beregning

Cross-team rule library for beregning (income calculation for benefits). 

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context
| Topic      | Details                                                                |
|------------|------------------------------------------------------------------------|
| Role       | Owns beregningsgrunnlag model and rules                                |
| Consumers  | `fp-kalkulus`, `fp-sak` (frontend/letters), `ft-kalkulus` (K9)         |
| Tech stack | Java SemVer library using `fp-nare` rule framework                     |
| Modules    | `kodeverk`, `kontrakt`, `beregningsregler`, `kalkulator`               |
| API        | `KalkulatorInterface` (operation), `KalkulatorGuiInterface` (frontend) |

Shared with team `k9saksbehandling`. 
Two-phase pipeline: (1) calculate beregningsgrunnlag from aktiviteter per ftrl. kap. 8, 9 and 14, (2) distribute over aktiviteter based on refusjon (reimbursement). 
Steps may produce avklaringsbehov - adressed by case workers though aksjonspunkt in  `fp-sak` or `k9-sak`.

## Verification

- Verify contract or rule changes through `fp-kalkulus`.
- For integration impact, relevant `navikt/fp-autotest` suites are `fpkalkulus`, `fpsak`, and `verdikjede`.
