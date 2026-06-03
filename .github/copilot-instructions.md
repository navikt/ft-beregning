# ft-beregning

Cross-team rule library for the beregning process supporting Folketrygdloven 14-4, 14-7, and 9 (using chapter 8 beregning). 

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic      | Details                                                                   |
|------------|---------------------------------------------------------------------------|
| Role       | Owns the common beregningsgrunnlag model and rules                        |
| Consumers  | `fp-kalkulus`, `fp-sak` (frontend and letters), `ft-kalkulus` (K9)        |
| Tech stack | Java SemVer library using `fp-nare` rule framework                        |
| API        | `KalkulatorInterface` operation and `KalkulatorGuiInterface` for frontend |

Code is shared between teams `teamforeldrepenger` and `k9saksbehandling`

Modules:
- `kodeverk`: enums used by this repo and consumers
- `kontrakt`: models for API input and output + frontend models used by `ft-frontend-saksbehandling`
- `beregningsregler`: the nare beregning rules
- `kalkulator`: API and mapping data to and from rules

## Beregning flow and steps

Phase 1: define and calculate the beregning baseline (beregningsgrunnlag)
- Finding calculation data (skjæringstidspunkt) and income sources (aktiviteter)
- Check factual basis before calculation
- Calculate based in Folketrygdloven chapter 8.
- Calculate other aktiviteter depending on previous step
- Foreldrepenger only: rules for the pregnant unemployed (receiving dagpenger)
- Evaluate vilkår: deny if gross calculated income less than defined minimum

Phase 2: distribute the beregningsgrunnlag over aktiviteter based on reimbursement claims
- K9/Pleiepenger: evaluate effect of new income sources
- Evaluate reimbursment claims and identify needs for manual evaluation (avklaringsbehov)
- Distribute over aktiviteter
- Finalize and limit to 6 * grunnbeløp

Most steps may identifiy needs for manual evaluation (avklaringsbehov) - handled by consumers.

## Verification

- Verify contract or rule changes through `fp-kalkulus`.
- For integration impact, relevant `navikt/fp-autotest` suites are `fpkalkulus`, `fpsak`, and `verdikjede`.
