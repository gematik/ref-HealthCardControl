/*
 * Copyright (c) 2020 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.ti.healthcard.control.c2c;

import cardfilesystem.*;
import cardfilesystem.egk21mf.PrK;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.IHealthCardType;
import de.gematik.ti.healthcardaccess.cardobjects.Key;
import de.gematik.ti.healthcardaccess.cardobjects.Password;
import de.gematik.ti.healthcardaccess.cardobjects.ShortFileIdentifier;
import de.gematik.ti.healthcardaccess.healthcards.*;

/**
 * Supplier the required parameter for Card2Card-Authentication
 */
public class CardFileSystemData {
    ShortFileIdentifier cvcAutrSfid;
    ShortFileIdentifier gdo;
    Password pinChRef;
    ShortFileIdentifier cvcCASfid;
    Key key;

    // could be improved by attaching cardfilesystem to cardtype in healtcard access
    static CardFileSystemData getCardFileSystemData(final IHealthCard card) {
        final CardFileSystemData cfs = new CardFileSystemData();
        final HealthCardStatusValid hcv = (HealthCardStatusValid) card.getStatus();
        final IHealthCardType type = hcv.getHealthCardType();
        if (type instanceof Egk2) {
            cfs.pinChRef = new Password(Egk2FileSystem.PIN.CH.PWID);
            cfs.cvcCASfid = new ShortFileIdentifier(Egk2FileSystem.EF.C_CA_EGK_CS_E256.SFID);
            cfs.gdo = new ShortFileIdentifier(Egk2FileSystem.EF.GDO.SFID);
            cfs.cvcAutrSfid = new ShortFileIdentifier(Egk2FileSystem.EF.C_EGK_AUT_CVC_E256.SFID);
            cfs.key = new Key(PrK.EgkAutCvcE256.KID);
        } else if (type instanceof Egk21) {
            cfs.pinChRef = new Password(Egk21FileSystem.PIN.CH.PWID);
            cfs.cvcCASfid = new ShortFileIdentifier(Egk21FileSystem.EF.C_CA_EGK_CS_E256.SFID);
            cfs.gdo = new ShortFileIdentifier(Egk21FileSystem.EF.GDO.SFID);
            cfs.cvcAutrSfid = new ShortFileIdentifier(Egk21FileSystem.EF.C_EGK_AUT_CVC_E256.SFID);
            cfs.key = new Key(cardfilesystem.egk21mf.PrK.EgkAutCvcE256.KID);
        } else if (type instanceof Hba2) {
            cfs.pinChRef = new Password(cardfilesystem.Hba2FileSystem.PIN.CH.PWID);
            cfs.cvcCASfid = new ShortFileIdentifier(Hba2FileSystem.EF.C_CA_HPC_CS_E256.SFID);
            cfs.gdo = new ShortFileIdentifier(Hba2FileSystem.EF.GDO.SFID);
            cfs.cvcAutrSfid = new ShortFileIdentifier(Hba2FileSystem.EF.C_HPC_AUTR_CVC_E256.SFID);
            cfs.key = new Key(cardfilesystem.hba2mf.PrK.HpcAutrCvcE256.KID);
        } else if (type instanceof Hba21) {
            cfs.pinChRef = new Password(cardfilesystem.Hba21FileSystem.PIN.CH.PWID);
            cfs.cvcCASfid = new ShortFileIdentifier(Hba21FileSystem.EF.C_CA_HPC_CS_E256.SFID);
            cfs.gdo = new ShortFileIdentifier(Hba21FileSystem.EF.GDO.SFID);
            cfs.cvcAutrSfid = new ShortFileIdentifier(Hba21FileSystem.EF.C_HPC_AUTR_CVC_E256.SFID);
            cfs.key = new Key(cardfilesystem.hba21mf.PrK.HpcAutrCvcE256.KID);
        } else if (type instanceof Smcb2) {
            cfs.pinChRef = new Password(cardfilesystem.Smcb2FileSystem.PIN.SMC.PWID);
            cfs.cvcCASfid = new ShortFileIdentifier(Smcb2FileSystem.EF.C_CA_SMC_CS_E256.SFID);
            cfs.gdo = new ShortFileIdentifier(Smcb2FileSystem.EF.GDO.SFID);
            cfs.cvcAutrSfid = new ShortFileIdentifier(Smcb2FileSystem.EF.C_SMC_AUTR_CVC_E256.SFID);
            cfs.key = new Key(cardfilesystem.smcb2mf.PrK.SmcAutrCvcE256.KID);
        } else if (type instanceof Smcb21) {
            cfs.pinChRef = new Password(cardfilesystem.Smcb21FileSystem.PIN.SMC.PWID);
            cfs.cvcCASfid = new ShortFileIdentifier(Smcb21FileSystem.EF.C_CA_SMC_CS_E256.SFID);
            cfs.gdo = new ShortFileIdentifier(Smcb21FileSystem.EF.GDO.SFID);
            cfs.cvcAutrSfid = new ShortFileIdentifier(Smcb21FileSystem.EF.C_SMC_AUTR_CVC_E256.SFID);
            cfs.key = new Key(cardfilesystem.smcb21mf.PrK.SmcAutrCvcE256.KID);
        }
        return cfs;
    }
}
