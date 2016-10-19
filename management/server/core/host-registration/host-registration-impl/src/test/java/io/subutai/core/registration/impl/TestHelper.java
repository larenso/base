package io.subutai.core.registration.impl;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;


public class TestHelper
{

    public static final String PGP_PUBLIC_KEY =
            "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" + "Version: Subutai Social v2.0.7\n"
                    + "Comment: https://subutai.io/\n" + "\n"
                    + "xsFNBFdG/LUBEADMXXiRtIJIdUnt2Ok1zw36X4TNOhCVz+fh/qoxiwI6zwvw\n"
                    + "Dw4+CZafsDd7V7QwsufCjFUPvIVCR4R4MX7OYSFyFI0qH7Gpg9l3RuZ8IwRk\n"
                    + "UouFxAmZnRU0urBKP+4MVAUb9XW+1BcvTIB9uDsBDHRyChHqmjzJxKJSQK2r\n"
                    + "7QliXMcZ0a+ncI5RTQ59y6mCTPBCUW2+dJaG2hfjkEyvYfcX/XtT3/6VG3Qs\n"
                    + "0iXEc+ZPo9r8LxegULk4eEx2u9hIiAFCTgHkZO6LAa4DRKx1gKeZgIpurs1r\n"
                    + "mkAT9d46gy9iSIR3ipoCJE46QFAJcDT9b87tpDAYMVfFLmqB41zTZgH8EjQl\n"
                    + "cQYhxXRC9E8TStinT3BlXyCH0iMM9IoK+VSAVQWIJG4l28isgwcknPix5atL\n"
                    + "jYHjM9oxRkUqdi1ML95NMwWoty7J26kfBnictrY7npZtENEJCOWpfItHpn8T\n"
                    + "gRlII5FGtLnticDDa++eILSdrxvxMbjA2zH1W0ixeN9YpIFNRJqujPMY4TnE\n"
                    + "1Rl1+XrRRPiHVybyXAHBIyeGrW+fiWbKcHL7J+yINKOhyd+6Whe0LXJHF7Sp\n"
                    + "lCVMBty2DbrergbvEil0i+jXqCIWRk75BV71EPl9LBJI7B1Bq+p+hyVJSRrB\n"
                    + "ENYCmj0hKhBPWu2oj5vY5C2IgUPRm14r+Vz+/wARAQABzRR1c2VyIDx1c2Vy\n"
                    + "QG1haWwuY29tPsLBdQQQAQgAKQUCV0b8twYLCQgHAwIJEJbKPoFDux0UBBUI\n"
                    + "AgoDFgIBAhkBAhsDAh4BAACXnw//cHH98ep9fQxtqaNEb96oV0J6aM0hAu44\n"
                    + "/+7sX5OSQLyfIlDFMrK3qsxXYeYuSPdxmb4P5KD44qsbYI2+aM3d7HnMi3Mf\n"
                    + "oVxSIbUWuCvOATMg32zNBsfS0NwKTucARPvrPQ1uWG7YbFagyTnNwBRRqrq1\n"
                    + "nZli5LBbMD7JdNMzwJ5srBQj0MkkXWxP8k7L3F+eYN1j0FFsII1rmqx68FWg\n"
                    + "3BjxxRdEKmsPwtjwNtzHcClAewTiedY9TEHhJcQrkx2prrC/tQlOW51joNBP\n"
                    + "bQGHCeBaiZRfMkc1uap0MJqUBsy5PYhQjxtqSteVGNeSXMpSVDYMkmQzRrP4\n"
                    + "VuZ3UqPu8uJbbTXOE2Z2UYCDX6YoxBnTLGi3MZfZitbktAKEvWw7wt2jI7fM\n"
                    + "Kme6pxwPgUcyRLg4HVLcGS8Lj5w00yRucrR+FHX7fBtiz9Yh2y4u8mrc9LEw\n"
                    + "U4wF1bOXQ5+FZizXwe7cTFLkSUZpfZwirznLbspAICwgTvPral3lQ9zcO1Vl\n"
                    + "lDkHaPMPYoLqIzGKr+CuQVCXW53NlRxTyoEK/qFnksZjIqE1lCxqKyHQ/Bbd\n"
                    + "FkxUfSCdpVdUkW9TbMxpKymEHf3kDbxLRZcYAIDHON+XHjTjs0wFI/QfMf4N\n"
                    + "6pCqpgEXXRHq5p5+MYHHme3pXnWompxQXNjt47yWOL5J3oG8gVrOwU0EV0b8\n"
                    + "tQEQAPFYH2aKfG2OT7uVR49Hm3HlJy1GWaaIuPunccB3s2XjEItT5vGmtxB5\n"
                    + "H22g26I81TVxnnB9zarSjsk+2Y3Lp7bNufiPXkVSSs+5hd2X/IhMkALQTKdp\n"
                    + "WnewfEOsK3TxOzkMBwqFd76VfJkFJoewxrlyl7dXwayvxfOBwxVeRRVzCYcl\n"
                    + "7D9Y8x9o3f2/GRotkCjbo3vdFTzq3jvpZ3GKfgflZSVoi9ZslvrYoRoPpnPb\n"
                    + "wnJLxcy0NJzfR3O0YHpJbFOPTyKV+BQf9R2RMpAdCjbwQm60UVn7NEzz5XaK\n"
                    + "98pbdnaceJFdjjUohgOt6S0tHhe7ManUO5RXPOc0qctfWbGAcERjyp6kmYjV\n"
                    + "jga2D0Rzh6Ureqx/+sYJsCDJvIFyLO+5J6pVF/ofZY4szKrNlezTnwF1K1E6\n"
                    + "VZXDro+NVsfa8Y0t9KPmAeT1hS2KJ9WuAEpoIsUAL000pItk1UVe9kk7k7N1\n"
                    + "Db7pw66rYs63NlAi6Epo5/9DrU/aMlSO04t5eAuR3mX/Dz4a0YpeqaISzTru\n"
                    + "2J1iKTJ3dT7jKWeMPPGOmrbI4IB5/JjBJIEgXKSwhgv0oE9+QPqJvSu3BMtF\n"
                    + "BMP0zbM8s+im4VJQ0R/E+SyJlV6OOfCjSfGl88g9iTES9AyYY1CL8zKdsHaJ\n"
                    + "yMCNTVKJyUIJFqKYbuCcCToyUPJTABEBAAHCwV8EGAEIABMFAldG/LgJEJbK\n"
                    + "PoFDux0UAhsMAAAsSA//Z4ohp2GdALVvuizE1H8MAX06djz9Xqm+Me8T767s\n"
                    + "neGU/vf3uV0/1ql5/fFHsP+/2YJZePqAZT1yoJwb70g/D4yJ+ABpcJ5g3e0k\n"
                    + "588LqqLmppChnc+3cv+SLwfDMuqzk9PYwqYIMt+uHUhgu89z0dh+hjXoM3X1\n"
                    + "Uq30i0pX+umEkmKUnQoq60ZIdCo5Q/zZvj6KhM9CXnqEs6Ny7Ex64tCynLHm\n"
                    + "85paNZlETXhbMRTuMahBof/ziv1Jl6WGGJo4tK/Z3c4QPurIzaFFqpnp1jp3\n"
                    + "DKhG601NTvYzzOs8VBLQqTbJZh+aV/I1u0YM9ULtfOpcip/WX0rE+e0K2H1Y\n"
                    + "3HzXn7TLYu8+BKvz9MeTfozF9ldsR2z8WPeNYU5Bq33r13EYZwN0uanIy40r\n"
                    + "RiP3oRw/C+juwWBgRUm87tksIGQxabK5wEiJ4reVh2FLYhvbmhd1tSH2RBET\n"
                    + "ix9p+Y/JD6t7Kb8w9Y9n6qh3W2L9xWZ5IKRvUcwbVI/r4hhuj4L3A45HV/ye\n"
                    + "tvrx11scRPI0upc04aH7kSvvqzlMav+zw9wVqH4UTCgPk44qZHvCrCc0yOwL\n"
                    + "Eaa7HmJ7G83/2iz7U9IERlWgwYR6/bEIs9VYLlP6wOTlQ93rTqDW3L8OztTT\n"
                    + "7N+Eij0SnaFX+ztUXyPSPAXzQHMaft4fPfJpiC3DRR8=\n" + "=ZLYo\n"
                    + "-----END PGP PUBLIC KEY BLOCK-----";


    public static PGPPublicKeyRing PGP_PUB_KEY() throws PGPException, IOException
    {

        PGPPublicKeyRingCollection pgpPublicKeyRings = new PGPPublicKeyRingCollection(
                PGPUtil.getDecoderStream( new ByteArrayInputStream( PGP_PUBLIC_KEY.getBytes() ) ),
                new JcaKeyFingerprintCalculator() );


        return pgpPublicKeyRings.getPublicKeyRing( pgpPublicKeyRings.iterator().next().getPublicKey().getKeyID() );
    }
}
